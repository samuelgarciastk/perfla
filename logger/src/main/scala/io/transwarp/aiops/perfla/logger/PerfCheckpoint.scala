package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, TaskIdentifier}

class PerfCheckpoint {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var startTime: Long = _
  private[logger] var endTime: Long = _
  private[logger] var totalTime: Long = _
  private[logger] var dataSize: Long = _
  private[logger] var taskCount: Int = _
  private var currentTime: Long = _

  reset()

  def reset(): Unit = {
    startTime = -1L
    endTime = -1L
    totalTime = 0L
    dataSize = 0L
    taskCount = 0
  }

  def setId(value: String): PerfCheckpoint = {
    id = value
    this
  }

  def setTaskIdentifier(value: TaskIdentifier): PerfCheckpoint = {
    taskIdentifier = value
    this
  }

  def start: PerfCheckpoint = {
    if (Config.setting.loggerEnable) {
      currentTime = System.nanoTime
      if (startTime == -1L) startTime = System.currentTimeMillis
    }
    this
  }

  def stop: PerfCheckpoint = stop(true)

  def stop(isTaskStop: Boolean): PerfCheckpoint = {
    if (Config.setting.loggerEnable) {
      endTime = System.currentTimeMillis
      totalTime += System.nanoTime - currentTime
      if (isTaskStop) taskCount += 1
    }
    this
  }

  def setSize(value: Long): PerfCheckpoint = {
    dataSize += value
    this
  }

  def isValid: Boolean = startTime != -1L && endTime != -1L
}
