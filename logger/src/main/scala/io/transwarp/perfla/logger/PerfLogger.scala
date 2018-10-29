package io.transwarp.perfla.logger

import io.transwarp.perfla.loader.{Config, Task, TaskIdentifier}
import org.slf4j.LoggerFactory

class PerfLogger(clazz: Class[_] = classOf[PerfLogger]) {
  private val logger = LoggerFactory.getLogger(clazz)
  private var isEnable: Boolean = true

  def checkpoint: Checkpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, null)
  }

  def checkpoint(className: String, methodName: String, uuid: String): Checkpoint = {
    val checkpoint = new Checkpoint
    val identifier = new TaskIdentifier(className, methodName)
    Config.identifierMap.get(identifier).foreach(task => {
      checkpoint.task = task
      checkpoint.uuid = uuid
      checkpoint.startTime = System.currentTimeMillis
    })
    checkpoint
  }

  def checkpoint(uuid: String): Checkpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, uuid)
  }

  def log(checkpoint: Checkpoint): Unit = {
    checkpoint.endTime = System.currentTimeMillis
    if (isEnable && checkpoint.task != null) {
      val diff = checkpoint.endTime - checkpoint.startTime
      diff match {
        case d if d > checkpoint.task.threshold.error * checkpoint.dataSize =>
          logger.error(s"${Config.setting.prefix} ${checkpoint.task.pattern}" +
            s" [ERROR]" +
            s" [${checkpoint.uuid}]" +
            s" [${checkpoint.startTime}~${checkpoint.endTime}:$diff]" +
            s" [${checkpoint.dataSize}]")
        case d if d > checkpoint.task.threshold.warn * checkpoint.dataSize =>
          logger.warn(s"${Config.setting.prefix} ${checkpoint.task.pattern}" +
            s" [WARN]" +
            s" [${checkpoint.uuid}]" +
            s" [${checkpoint.startTime}~${checkpoint.endTime}:$diff]" +
            s" [${checkpoint.dataSize}]")
        case _ =>
      }
    }
  }

  def setEnable(value: Boolean): Unit = isEnable = value
}

class Checkpoint {
  var uuid: String = _
  var startTime: Long = _
  var endTime: Long = _
  var task: Task = _
  var dataSize: Long = 1

  def setUUID(id: String): Unit = uuid = id

  def setDataSize(size: Long): Unit = dataSize = size
}

object PerfLogger {
  def getLogger: PerfLogger = new PerfLogger

  def getLogger(clazz: Class[_]): PerfLogger = new PerfLogger(clazz)
}
