package io.transwarp.aiops.perfla.loader

import java.io.{File, FileInputStream}
import java.nio.file._

import io.transwarp.aiops.perfla.loader.monitor.SysMonitor
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
  var taskIdMap: mutable.Map[String, Task] = _
  private var hasDaemon = false
  private var valid = false

  load()

  def isValid: Boolean = valid

  private def load(): Unit = logger.synchronized {
    if (new File(configPath).exists) {
      logger.info(s"PerfLA-loader: Config path [$configPath].")
      val configInput = new FileInputStream(configPath)
      try {
        val config = yaml.load(configInput).asInstanceOf[ConfigBean]
        // settings
        val newSetting = new Setting
        newSetting.init(config.settings)
        setting = newSetting
        // tasks
        val tasks = config.tasks
        if (tasks == null) {
          logger.warn(s"PerfLA-loader: No task is defined in the config file.")
          valid = false
          startWatchDaemon()
        } else {
          val newIdentifierMap = new mutable.HashMap[TaskIdentifier, Task]
          val newTaskIdMap = new mutable.HashMap[String, Task]

          tasks.foreach(taskBean => {
            val identifier = new TaskIdentifier(taskBean.clazz, taskBean.method)
            val task = new Task
            task.init(identifier, taskBean)
            newIdentifierMap += identifier -> task
            newTaskIdMap += task.id -> task
          })

          identifierMap = newIdentifierMap
          taskIdMap = newTaskIdMap
          genTaskRelation()

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

  private def genTaskRelation(): Unit = {
    val roots = new mutable.HashSet[String]
    roots ++= taskIdMap.keySet

    def recurse(t: Task): mutable.HashSet[String] = if (t.hasSubTasks) {
      if (t.allSubTaskIds == null) {
        t.allSubTaskIds = new mutable.HashSet[String]
        t.allSubTaskIds ++= t.subTaskIds
        t.allSubTaskIds ++= t.subTaskIds.map(subTaskId => recurse(taskIdMap(subTaskId))).reduce(_ ++ _)
      }
      roots --= t.subTaskIds
      t.allSubTaskIds
    } else new mutable.HashSet

    roots.foreach(f => recurse(Config.taskIdMap(f)))

    // Create root task
    val rootTask = new Task
    assert(roots.nonEmpty)
    rootTask.hasSubTasks = true
    rootTask.subTaskIds = roots
    rootTask.allSubTaskIds = taskIdMap.keySet.to[mutable.HashSet]
    taskIdMap += "root" -> rootTask
  }

  def startWatchDaemon(): Unit = {
    SysMonitor.start()
    if (!hasDaemon) {
      daemon.stop = false
      val t = new Thread(daemon)
      t.setDaemon(true)
      t.start()
      hasDaemon = true
      logger.info("PerfLA-loader: Config watcher started.")
    }
  }

  def stopWatchDaemon(): Unit = {
    daemon.stop = true
    SysMonitor.stop()
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
      while (!stop) {
        val watchKey = watchService.take
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
    }
  }

}
