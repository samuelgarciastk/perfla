package io.transwarp.perfla.analyzer

import io.transwarp.perfla.analyzer.TaskLevel.TaskLevel
import io.transwarp.perfla.loader.{Config, Task}

class TaskEntry(line: String) {
  var task: Task = _
  var level: TaskLevel = _
  var uuid: String = _
  var startTime: Long = _
  var endTime: Long = _
  var diff: Long = _
  var dataSize: Long = _

  init

  private def init: Unit = {
    var startIdx = line.indexOf(Config.setting.prefix) + Config.setting.prefix.length + 1
    var endIdx = line.indexOf('[', startIdx)
    val pattern = line.substring(startIdx, endIdx - 1)
    task = Config.patternMap.get(pattern).orNull
    startIdx = endIdx + 1
    endIdx = line.indexOf(']', startIdx)
    level = TaskLevel.withName(line.substring(startIdx, endIdx))
    startIdx = endIdx + 3
    endIdx = line.indexOf(']', startIdx)
    uuid = line.substring(startIdx, endIdx)
    startIdx = endIdx + 3
    endIdx = line.indexOf('~', startIdx)
    startTime = line.substring(startIdx, endIdx).toLong
    startIdx = endIdx + 1
    endIdx = line.indexOf(':', startIdx)
    endTime = line.substring(startIdx, endIdx).toLong
    startIdx = endIdx + 1
    endIdx = line.indexOf(']', startIdx)
    diff = line.substring(startIdx, endIdx).toLong
    startIdx = endIdx + 3
    endIdx = line.indexOf(']', startIdx)
    dataSize = line.substring(startIdx, endIdx).toLong
  }
}

object TaskLevel extends Enumeration {
  type TaskLevel = Value
  val WARN = Value("WARN")
  val ERROR = Value("ERROR")
}
