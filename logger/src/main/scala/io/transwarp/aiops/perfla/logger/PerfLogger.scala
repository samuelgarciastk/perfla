package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.LogMod.LogMod
import io.transwarp.aiops.perfla.loader.{Config, LogMod, TaskIdentifier}
import org.slf4j.LoggerFactory

class PerfLogger(clazz: Class[_] = classOf[PerfLogger]) {
  private val logger = LoggerFactory.getLogger(clazz)

  def checkpoint: Checkpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, null)
  }

  def checkpoint(id: String): Checkpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, id)
  }

  def checkpoint(className: String, methodName: String, id: String): Checkpoint = (new Checkpoint)
    .setId(id)
    .setTaskIdentifier(new TaskIdentifier(className, methodName))
    .start

  def log(checkpoint: Checkpoint): Unit = log(checkpoint, null)

  def log(checkpoint: Checkpoint, logMod: LogMod): Unit = if (Config.isValid) {
    if (!checkpoint.isValid) checkpoint.stop
    checkpoint.initTask
    if (checkpoint.task != null) {
      Option(logMod).getOrElse(checkpoint.task.mode) match {
        case LogMod.DEFAULT => defaultLog(checkpoint)
        case LogMod.FORCE => forceLog(checkpoint)
        case LogMod.MUTE =>
        case _ => logger.warn("Unknown PerfLA log mod!")
      }
    }
  }

  private def defaultLog(checkpoint: Checkpoint): Unit = {
    checkpoint.interval match {
      case i if i > checkpoint.task.threshold.error * checkpoint.dataSize =>
        logger.error(s"${Config.setting.prefix}" +
          s" ${checkpoint.task.pattern}" +
          s" [ERROR]" +
          s" [${checkpoint.id}]" +
          s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
          s" [${checkpoint.dataSize}]")
      case i if i > checkpoint.task.threshold.warn * checkpoint.dataSize =>
        logger.warn(s"${Config.setting.prefix}" +
          s" ${checkpoint.task.pattern}" +
          s" [WARN]" +
          s" [${checkpoint.id}]" +
          s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
          s" [${checkpoint.dataSize}]")
      case _ =>
    }
  }

  private def forceLog(checkpoint: Checkpoint): Unit = {
    logger.info(s"${Config.setting.prefix}" +
      s" ${checkpoint.task.pattern}" +
      s" [INFO]" +
      s" [${checkpoint.id}]" +
      s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
      s" [${checkpoint.dataSize}]")
  }
}

object PerfLogger {
  def getLogger: PerfLogger = new PerfLogger

  def getLogger(clazz: Class[_]): PerfLogger = new PerfLogger(clazz)
}
