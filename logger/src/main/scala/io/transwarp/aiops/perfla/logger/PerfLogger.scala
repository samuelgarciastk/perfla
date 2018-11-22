package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.LogMod.LogMod
import io.transwarp.aiops.perfla.loader._
import org.slf4j.LoggerFactory

class PerfLogger(clazz: Class[_] = classOf[PerfLogger]) {
  private val logger = LoggerFactory.getLogger(clazz)

  def checkpoint(id: String): PerfCheckpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, id)
  }

  def checkpoint(clazz: String, method: String, id: String): PerfCheckpoint = (new PerfCheckpoint)
    .setId(id)
    .setTaskIdentifier(new TaskIdentifier(clazz, method))

  def checkpoint(clazz: String, method: String): PerfCheckpoint = (new PerfCheckpoint)
    .setTaskIdentifier(new TaskIdentifier(clazz, method))

  def log(checkpoint: PerfCheckpoint): Unit = log(checkpoint, null)

  def log(checkpoint: PerfCheckpoint, logMod: LogMod): Unit =
    if (Config.isValid && Config.setting.loggerEnable && checkpoint.isValid) {
      val task = getTask(checkpoint.taskIdentifier)
      if (task != null) {
        Option(logMod).getOrElse(task.mode) match {
          case LogMod.DEFAULT => defaultLog(checkpoint, task)
          case LogMod.FORCE => forceLog(checkpoint, task)
          case LogMod.MUTE =>
          case _ => logger.warn("Unknown PerfLA log mod.")
        }
      } else if (logMod == LogMod.FORCE) {
        forceLog(checkpoint)
      }
    }

  private def defaultLog(checkpoint: PerfCheckpoint, task: Task): Unit = {
    val dataSize = if (checkpoint.dataSize == 0L) 1L else checkpoint.dataSize
    checkpoint.totalTime match {
      case i if i > task.threshold.error * dataSize =>
        logger.error(logFormat(
          task.pattern,
          task.id,
          TaskLevel.ERROR.toString,
          checkpoint.id,
          checkpoint.startTime,
          checkpoint.endTime,
          checkpoint.totalTime,
          checkpoint.dataSize,
          checkpoint.taskCount))
      case i if i > task.threshold.warn * dataSize =>
        logger.warn(logFormat(
          task.pattern,
          task.id,
          TaskLevel.WARN.toString,
          checkpoint.id,
          checkpoint.startTime,
          checkpoint.endTime,
          checkpoint.totalTime,
          checkpoint.dataSize,
          checkpoint.taskCount))
      case _ =>
    }
  }

  private def logFormat(pattern: String,
                        taskId: String,
                        level: String,
                        id: String,
                        startTime: Long,
                        endTime: Long,
                        totalTime: Long,
                        dataSize: Long,
                        taskNum: Int): String =
    s"${Config.setting.prefix}|$pattern|$taskId|$level|$id|$startTime|$endTime|$totalTime|$dataSize|$taskNum"

  private def forceLog(checkpoint: PerfCheckpoint, task: Task = null): Unit = {
    logger.info(logFormat(
      if (task == null) "Unknown Task" else task.pattern,
      if (task == null) "UnknownId" else task.id,
      TaskLevel.INFO.toString,
      checkpoint.id,
      checkpoint.startTime,
      checkpoint.endTime,
      checkpoint.endTime - checkpoint.startTime,
      checkpoint.dataSize,
      checkpoint.taskCount))
  }

  private def getTask(taskIdentifier: TaskIdentifier): Task = {
    if (taskIdentifier != null && Config.isValid) {
      Config.identifierMap.get(taskIdentifier).orNull
    } else null
  }
}

object PerfLogger {
  def getLogger: PerfLogger = new PerfLogger

  def getLogger(clazz: Class[_]): PerfLogger = new PerfLogger(clazz)
}
