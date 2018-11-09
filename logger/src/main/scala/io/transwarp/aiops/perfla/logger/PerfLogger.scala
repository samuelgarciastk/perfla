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
        logger.error(s"${Config.setting.prefix}" +
          s" ${task.pattern}" +
          s" [ERROR]" +
          s" [${checkpoint.id}]" +
          s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
          s" [${checkpoint.dataSize}]")
      case i if i > task.threshold.warn * dataSize =>
        logger.warn(s"${Config.setting.prefix}" +
          s" ${task.pattern}" +
          s" [WARN]" +
          s" [${checkpoint.id}]" +
          s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
          s" [${checkpoint.dataSize}]")
      case _ =>
    }
  }

  private def forceLog(checkpoint: Checkpoint, task: Task): Unit = {
    logger.info(s"${Config.setting.prefix}" +
      s" ${if (task == null) "Unknown Task" else task.pattern}" +
      s" [INFO]" +
      s" [${checkpoint.id}]" +
      s" [${checkpoint.startTime}~${checkpoint.endTime}:${checkpoint.interval}]" +
      s" [${checkpoint.dataSize}]")
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
        logger.error(s"${Config.setting.prefix}" +
          s" ${task.pattern}" +
          s" [ERROR]" +
          s" [${collector.id}]" +
          s" [${collector.startTime}~${collector.endTime}:${collector.totalTime}]" +
          s" [${collector.dataSize}]")
      case t if t > task.threshold.warn * dataSize =>
        logger.warn(s"${Config.setting.prefix}" +
          s" ${task.pattern}" +
          s" [WARN]" +
          s" [${collector.id}]" +
          s" [${collector.startTime}~${collector.endTime}:${collector.totalTime}]" +
          s" [${collector.dataSize}]")
      case _ =>
    }
  }

  private def forceLog(collector: Collector, task: Task): Unit = {
    logger.info(s"${Config.setting.prefix}" +
      s" ${if (task == null) "Unknown Task" else task.pattern}" +
      s" [INFO]" +
      s" [${collector.id}]" +
      s" [${collector.startTime}~${collector.endTime}:${collector.totalTime}]" +
      s" [${collector.dataSize}]")
  }
}

object PerfLogger {
  def getLogger: PerfLogger = new PerfLogger

  def getLogger(clazz: Class[_]): PerfLogger = new PerfLogger(clazz)
}
