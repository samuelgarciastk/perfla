package io.transwarp.aiops.perfla.loader

class Task(val taskIdentifier: TaskIdentifier, taskBean: TaskBean) {
  val id: String = Option(taskBean.id).getOrElse("no-id")
  val pattern: String = Option(taskBean.pattern).getOrElse(throw new IllegalArgumentException("PerfLA-loader: Undefined task pattern."))
  /*
  0000|Mem|CPU|IO Read|IO Write
   */
  val taskType: Byte = {
    if (taskBean.task_type != null) {
      var b = 0
      taskBean.task_type.foreach {
        case "IO_W" => b |= 1
        case "IO_R" => b |= 1 << 1
        case "CPU" => b |= 1 << 2
        case "Mem" => b |= 1 << 3
      }
      b.toByte
    } else throw new IllegalArgumentException("PerfLA-loader: Undefined task type.")
  }
  val threshold: Threshold = new Threshold(taskType, Option(taskBean.warn_factor).getOrElse(1D), Option(taskBean.error_factor).getOrElse(2D))
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

class Threshold(taskType: Byte, warnFactor: Double, errorFactor: Double) {
  var warn: Double = _
  var error: Double = _

  init

  private def init: Unit = {
    warn = threshold(warnFactor)
    error = threshold(errorFactor)
  }

  private def threshold(factor: Double): Double = {
    var t = 0D
    if ((taskType & 1) == 1) {
      t += factor / Config.setting.io_write
    }
    if ((taskType >> 1 & 1) == 1) {
      t += factor / Config.setting.io_read
    }
    if ((taskType >> 2 & 1) == 1) {
      // CPU
    }
    if ((taskType >> 3 & 1) == 1) {
      // Mem
    }
    t
  }
}
