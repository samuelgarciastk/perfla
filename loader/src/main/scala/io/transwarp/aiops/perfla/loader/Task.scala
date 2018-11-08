package io.transwarp.aiops.perfla.loader

import io.transwarp.aiops.perfla.loader.LogMod.LogMod

class Task {
  var id: String = _
  var identifier: TaskIdentifier = _
  var pattern: String = _
  var mode: LogMod = _
  var threshold: Threshold = _
  var subTasks: Array[String] = _

  def init(taskIdentifier: TaskIdentifier, taskBean: TaskBean): Unit = {
    id = Option(taskBean.id).getOrElse(Task.default_id)
    identifier = taskIdentifier
    pattern = Option(taskBean.pattern).getOrElse(Task.defaultPattern(taskIdentifier))
    mode = LogMod.withNameWithDefault(taskBean.mode)
    threshold = new Threshold
    threshold.init(taskBean.threshold)
    subTasks = taskBean.sub_tasks
  }
}

object Task {
  private val default_id = "no-id"

  private def defaultPattern(taskIdentifier: TaskIdentifier): String = taskIdentifier.className + " " + taskIdentifier.methodName
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
