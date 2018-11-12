package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.LogMod.LogMod
import io.transwarp.aiops.perfla.loader.{Config, LogMod, Task, TaskIdentifier}
import org.slf4j.LoggerFactory

class PerfLogger(clazz: Class[_] = classOf[PerfLogger]) {
  private val logger = LoggerFactory.getLogger(clazz)

  def checkpoint(id: String): Checkpoint = {
    val caller = (new Throwable).getStackTrace()(1)
    checkpoint(caller.getClassName, caller.getMethodName, id)
  }

  def checkpoint(clazz: String, method: String, id: String): Checkpoint = (new Checkpoint)
    .setId(id)
    .setTaskIdentifier(new TaskIdentifier(clazz, method))

  def collector(clazz: String, method: String): Collector = (new Collector)
    .setTaskIdentifier(new TaskIdentifier(clazz, method))

  def log(checkpoint: Checkpoint): Unit = log(checkpoint, null)

  def log(checkpoint: Checkpoint, logMod: LogMod): Unit = if (Config.isValid) {
    if (!checkpoint.isValid) checkpoint.stop
    val task = getTask(checkpoint.taskIdentifier)
    if (task != null) {
      Option(logMod).getOrElse(task.mode) match {
        case LogMod.DEFAULT => defaultLog(checkpoint, task)
        case LogMod.FORCE => forceLog(checkpoint, task)
        case LogMod.MUTE =>
        case _ => logger.warn("Unknown PerfLA log mod.")
      }
    } else if (logMod == LogMod.FORCE) {
      forceLog(checkpoint, null)
    }
  }

  private def defaultLog(checkpoint: Checkpoint, task: Task): Unit = {
    val dataSize = if (checkpoint.dataSize == 0) 1L else checkpoint.dataSize
    checkpoint.interval match {
      case i if i > task.threshold.error * dataSize =>
        logger.error(logFormat(
          task.pattern,
          "ERROR",
          checkpoint.id,
          checkpoint.startTime,
          checkpoint.endTime,
          checkpoint.interval,
          checkpoint.dataSize,
          1))
      case i if i > task.threshold.warn * dataSize =>
        logger.warn(logFormat(
          task.pattern,
          "WARN",
          checkpoint.id,
          checkpoint.startTime,
          checkpoint.endTime,
          checkpoint.interval,
          checkpoint.dataSize,
          1))
      case _ =>
    }
  }

  private def forceLog(checkpoint: Checkpoint, task: Task): Unit = {
    logger.info(logFormat(
      if (task == null) "Unknown Task" else task.pattern,
      "INFO",
      checkpoint.id,
      checkpoint.startTime,
      checkpoint.endTime,
      checkpoint.interval,
      checkpoint.dataSize,
      1))
  }

  def log(collector: Collector): Unit = log(collector, null)

  def log(collector: Collector, logMod: LogMod): Unit = if (Config.isValid && collector.isValid) {
    val task = getTask(collector.taskIdentifier)
    if (task != null) {
      Option(logMod).getOrElse(task.mode) match {
        case LogMod.DEFAULT => defaultLog(collector, task)
        case LogMod.FORCE => forceLog(collector, task)
        case LogMod.MUTE =>
        case _ => logger.warn("Unknown PerfLA log mod.")
      }
    } else if (logMod == LogMod.FORCE) {
      forceLog(collector, null)
    }
  }

  private def getTask(taskIdentifier: TaskIdentifier): Task = {
    if (taskIdentifier != null && Config.isValid) {
      Config.identifierMap.get(taskIdentifier).orNull
    } else null
  }

  private def defaultLog(collector: Collector, task: Task): Unit = {
    val dataSize = if (collector.dataSize == 0) 1L else collector.dataSize
    collector.totalTime match {
      case t if t > task.threshold.error * dataSize =>
        logger.error(logFormat(
          task.pattern,
          "ERROR",
          collector.id,
          collector.startTime,
          collector.endTime,
          collector.totalTime,
          collector.dataSize,
          collector.taskNum))
      case t if t > task.threshold.warn * dataSize =>
        logger.warn(logFormat(
          task.pattern,
          "WARN",
          collector.id,
          collector.startTime,
          collector.endTime,
          collector.totalTime,
          collector.dataSize,
          collector.taskNum))
      case _ =>
    }
  }

  private def logFormat(pattern: String,
                        level: String,
                        id: String,
                        startTime: Long,
                        endTime: Long,
                        totalTime: Long,
                        dataSize: Long,
                        taskNum: Int): String =
    s"${Config.setting.prefix}|$pattern|$level|$id|$startTime|$endTime|$totalTime|$dataSize|$taskNum"

  private def forceLog(collector: Collector, task: Task): Unit = {
    logger.info(logFormat(
      if (task == null) "Unknown Task" else task.pattern,
      "INFO",
      collector.id,
      collector.startTime,
      collector.endTime,
      collector.totalTime,
      collector.dataSize,
      collector.taskNum
    ))
  }
}

object PerfLogger {
  def getLogger: PerfLogger = new PerfLogger

  def getLogger(clazz: Class[_]): PerfLogger = new PerfLogger(clazz)
}
