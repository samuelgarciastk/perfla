package io.transwarp.aiops.perfla.analyzer

import io.transwarp.aiops.perfla.analyzer.TaskLevel.TaskLevel
import io.transwarp.aiops.perfla.loader.Config

private[analyzer] class TaskEntry(line: String) {
  var taskId: String = _
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
    if (elements.length == 10) {
      taskId = elements(2)
      level = TaskLevel.withNameWithDefault(elements(3))
      id = elements(4)
      startTime = elements(5).toLong
      endTime = elements(6).toLong
      totalTime = elements(7).toLong
      dataSize = elements(8).toLong
      taskNum = elements(9).toInt
    }
  }
}

private[analyzer] object TaskLevel extends Enumeration {
  type TaskLevel = Value
  val UNKNOWN, INFO, WARN, ERROR = Value

  def withNameWithDefault(name: String): Value = values.find(_.toString == name).getOrElse(UNKNOWN)
}
