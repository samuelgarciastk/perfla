package io.transwarp.aiops.perfla.analyzer

import io.transwarp.aiops.perfla.analyzer.TaskLevel.TaskLevel
import io.transwarp.aiops.perfla.loader.{Config, Task}

class TaskEntry(line: String) {
  var task: Task = _
  var level: TaskLevel = _
  var id: String = _
  var startTime: Long = _
  var endTime: Long = _
  var totalTime: Long = _
  var dataSize: Long = _
  var taskNum: Int = _

  init()

  private def init(): Unit = {
    val startIndex = line.indexOf(Config.setting.prefix)
    val elements = line.split("\\|", startIndex)
    if (elements.length == 9) {
      task = Config.patternMap.get(elements(1)).orNull
      level = TaskLevel.withNameWithDefault(elements(2))
      id = elements(3)
      startTime = elements(4).toLong
      endTime = elements(5).toLong
      totalTime = elements(6).toLong
      dataSize = elements(7).toLong
      taskNum = elements(8).toInt
    }
  }
}

object TaskLevel extends Enumeration {
  type TaskLevel = Value
  val UNKNOWN, WARN, ERROR = Value

  def withNameWithDefault(name: String): Value = values.find(_.toString == name).getOrElse(UNKNOWN)
}
