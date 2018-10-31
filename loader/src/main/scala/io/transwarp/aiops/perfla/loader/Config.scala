package io.transwarp.aiops.perfla.loader

import java.io.{File, FileInputStream}
import java.nio.file._

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.collection.JavaConverters._
import scala.collection.mutable

object Config {
  private val Logger = LoggerFactory.getLogger(Config.getClass)
  private val yaml = new Yaml(new Constructor(classOf[ConfigBean]))
  private val configFileName = "perfla-config.yml"
  private val configDirPath = new File(Config.getClass.getProtectionDomain.getCodeSource.getLocation.toURI)
    .getParentFile.getAbsolutePath
  private val configPath = s"$configDirPath/$configFileName"
  var setting: Setting = _
  var identifierMap: mutable.Map[TaskIdentifier, Task] = _
  var patternMap: mutable.Map[String, Task] = _
  private var valid = false

  {
    Logger.info(s"PerfLA-loader: Config path [$configPath].")
    load
    watchDaemon
  }

  def isValid: Boolean = valid

  private def watchDaemon: Unit = {
    val daemon = new Thread(new Runnable {
      override def run(): Unit = {
        Logger.info("PerfLA-loader: Start watcher.")
        val watchService = FileSystems.getDefault.newWatchService
        Paths.get(configDirPath).register(watchService,
          StandardWatchEventKinds.ENTRY_MODIFY,
          StandardWatchEventKinds.ENTRY_CREATE)
        var break = false
        while (!break) {
          val watchKey = watchService.take
          watchKey.pollEvents.asScala.foreach(event => {
            val changed = event.context.asInstanceOf[Path]
            if (changed.endsWith(configFileName)) {
              Logger.info("PerfLA-loader: Config file modified.")
              load
            }
          })
          if (!watchKey.reset) {
            break = true
            Logger.warn("PerfLA-loader: Watch key has been unregistered!")
          }
        }
      }
    })
    daemon.setDaemon(true)
    daemon.start()
  }

  private def load: Unit = {
    if (new File(configPath).exists) {
      val configInput = new FileInputStream(configPath)
      try {
        valid = true
        val config = yaml.load(configInput).asInstanceOf[ConfigBean]

        setting = new Setting(config.settings)

        val tasks = config.tasks
        identifierMap = new mutable.HashMap[TaskIdentifier, Task]()
        patternMap = new mutable.HashMap[String, Task]()
        tasks.foreach(f => {
          val identifier = new TaskIdentifier(f.class_name, f.method_name)
          val task = new Task(identifier, f)
          identifierMap += identifier -> task
          patternMap += task.pattern -> task
        })
      } catch {
        case e: Exception =>
          valid = false
          Logger.error("PerfLA-loader: Malformed config file.", e)
      }
    } else {
      valid = false
      Logger.warn(s"PerfLA-loader: Config file [$configPath] is not available.")
    }
  }
}
