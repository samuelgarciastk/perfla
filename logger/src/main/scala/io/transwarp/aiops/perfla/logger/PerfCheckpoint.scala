package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, TaskIdentifier}

class PerfCheckpoint {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var startTime: Long = -1L
  private[logger] var endTime: Long = -1L
  private[logger] var totalTime: Long = 0L
  private[logger] var dataSize: Long = 0L
  private[logger] var taskCount: Int = 0
  private var currentTime: Long = _

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
      if (startTime == -1L) startTime = currentTime
    }
    this
  }

  def stop: PerfCheckpoint = stop(true)

  def stop(isTaskStop: Boolean): PerfCheckpoint = {
    if (Config.setting.loggerEnable) {
      endTime = System.nanoTime
      totalTime += endTime - currentTime
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
