package io.transwarp.aiops.perfla.loader

import io.transwarp.aiops.perfla.loader.LogMod.LogMod

import scala.collection.mutable

class Task {
  var id: String = _
  var identifier: TaskIdentifier = _
  var pattern: String = _
  var mode: LogMod = _
  var threshold: Threshold = _
  var hasSubTasks = false
  var subTaskIds: mutable.HashSet[String] = _
  var allSubTaskIds: mutable.HashSet[String] = _

  def init(taskIdentifier: TaskIdentifier, taskBean: TaskBean): Unit = {
    id = Option(taskBean.id).getOrElse(throw new RuntimeException("PerfLA-loader: Task with undefined task id."))
    identifier = taskIdentifier
    pattern = Option(taskBean.pattern).getOrElse(Task.defaultPattern(taskIdentifier))
    mode = if (taskBean.mode == null) LogMod.DEFAULT else LogMod.withNameWithDefault(taskBean.mode)
    threshold = new Threshold(taskBean.threshold)
    if (taskBean.sub_tasks != null && taskBean.sub_tasks.nonEmpty) {
      hasSubTasks = true
      subTaskIds = new mutable.HashSet[String]
      subTaskIds ++= taskBean.sub_tasks
    }
  }

  def directContains(taskId: String): Boolean = if (hasSubTasks) subTaskIds.contains(taskId) else false

  def contains(taskId: String): Boolean = if (hasSubTasks) allSubTaskIds.contains(taskId) else false
}

object Task {
  val root_id = "root"

  def defaultPattern(taskIdentifier: TaskIdentifier): String = taskIdentifier.className + " " + taskIdentifier.methodName
}

class TaskIdentifier(val className: String, val methodName: String) {
  if (className == null || methodName == null) throw new IllegalArgumentException("PerfLA-loader: Null value in task identifier.")

  override def equals(obj: Any): Boolean = obj match {
    case o: TaskIdentifier =>
      o.className == this.className && o.methodName == this.methodName
    case _ => false
  }

  override def hashCode(): Int = (className + methodName).hashCode
}

object LogMod extends Enumeration {
  type LogMod = Value
  val UNKNOWN, DEFAULT, FORCE, MUTE = Value

  def withNameWithDefault(name: String): Value = values.find(_.toString == name).getOrElse(UNKNOWN)
}

object TaskLevel extends Enumeration {
  type TaskLevel = Value
  val UNKNOWN, INFO, WARN, ERROR = Value

  def withNameWithDefault(name: String): Value = values.find(_.toString == name).getOrElse(UNKNOWN)
}
