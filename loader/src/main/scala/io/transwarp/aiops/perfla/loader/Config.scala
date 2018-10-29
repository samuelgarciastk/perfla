package io.transwarp.aiops.perfla.loader

import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor

import scala.collection.mutable

object Config {
  private val configPath = "perfla-config.yml"
  private val Logger = LoggerFactory.getLogger(Config.getClass)

  var setting: Setting = _
  var identifierMap: mutable.Map[TaskIdentifier, Task] = _
  var patternMap: mutable.Map[String, Task] = _

  load

  private def load: Unit = {
    val configFile = getClass.getClassLoader.getResourceAsStream(configPath)
    if (configFile != null) {
      val yaml = new Yaml(new Constructor(classOf[ConfigBean]))
      val config = yaml.load(configFile).asInstanceOf[ConfigBean]

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
    } else {
      Logger.warn(s"Config file is not available: $configPath")
    }
  }
}
