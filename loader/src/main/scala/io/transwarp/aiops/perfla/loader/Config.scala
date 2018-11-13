package io.transwarp.aiops.perfla.loader

import java.io.{File, FileInputStream}
import java.nio.file._

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.collection.JavaConverters._
import scala.collection.mutable

object Config {
  private val logger = LoggerFactory.getLogger(Config.getClass)
  private val yaml = new Yaml(new Constructor(classOf[ConfigBean]))
  private val configFileName = "perfla-config.yml"
  private val configDirPath = new File(Config.getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    .getParentFile.getAbsolutePath
  private val configPath = s"$configDirPath/$configFileName"
  private val daemon = new ConfigWatcher
  var setting: Setting = _
  var identifierMap: mutable.Map[TaskIdentifier, Task] = _
  var patternMap: mutable.Map[String, Task] = _
  private var hasDaemon = false
  private var valid = false

  load()

  def isValid: Boolean = valid

  private def load(): Unit = {
    if (new File(configPath).exists) {
      logger.info(s"PerfLA-loader: Config path [$configPath].")
      val configInput = new FileInputStream(configPath)
      try {
        val config = yaml.load(configInput).asInstanceOf[ConfigBean]

        val newSetting = new Setting
        newSetting.init(config.settings)
        setting = newSetting

        val tasks = config.tasks
        if (tasks == null) {
          logger.warn(s"PerfLA-loader: No task is defined in the config file.")
          valid = false
          startWatchDaemon()
        } else {
          val newIdentifierMap = new mutable.HashMap[TaskIdentifier, Task]()
          val newPatternMap = new mutable.HashMap[String, Task]()
          tasks.foreach(taskBean => {
            val identifier = new TaskIdentifier(taskBean.clazz, taskBean.method)
            val task = new Task
            task.init(identifier, taskBean)
            newIdentifierMap += identifier -> task
            newPatternMap += task.pattern -> task
          })
          identifierMap = newIdentifierMap
          patternMap = newPatternMap
          valid = true
          if (setting.watcherEnable) startWatchDaemon()
          else stopWatchDaemon()
        }
      } catch {
        case e: Exception =>
          logger.error("PerfLA-loader: Malformed config file.", e)
          valid = false
          startWatchDaemon()
      }
    } else {
      logger.warn(s"PerfLA-loader: Config file [$configPath] is not available.")
      valid = false
      startWatchDaemon()
    }
  }

  def startWatchDaemon(): Unit = if (!hasDaemon) {
    daemon.stop = false
    val t = new Thread(daemon)
    t.setDaemon(true)
    t.start()
    hasDaemon = true
    logger.info("PerfLA-loader: Config watcher started.")
  }

  def stopWatchDaemon(): Unit = {
    daemon.stop = true
    hasDaemon = false
    logger.info("PerfLA-loader: Config watcher terminated.")
  }

  private class ConfigWatcher extends Runnable {
    var stop = false

    override def run(): Unit = {
      val watchService = FileSystems.getDefault.newWatchService
      Paths.get(configDirPath).register(watchService,
        StandardWatchEventKinds.ENTRY_MODIFY,
        StandardWatchEventKinds.ENTRY_CREATE)
      var watchKey: WatchKey = null
      while (!stop) {
        watchKey = watchService.take
        watchKey.pollEvents.asScala.foreach(event => {
          val changed = event.context.asInstanceOf[Path]
          if (changed.endsWith(configFileName)) {
            logger.info("PerfLA-loader: Config file modified.")
            load()
          }
        })
        if (!watchKey.reset) {
          stop = true
          logger.warn("PerfLA-loader: Watch key has been unregistered.")
        }
      }
      watchKey.cancel()
    }
  }

}
