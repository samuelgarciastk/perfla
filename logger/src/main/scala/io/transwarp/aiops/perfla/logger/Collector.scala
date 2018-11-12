package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.TaskIdentifier

class Collector {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var dataSize = 0L
  private[logger] var totalTime = 0L
  private[logger] var startTime = -1L
  private[logger] var endTime = -1L
  private[logger] var taskNum = 0
  private var currentTime: Long = _
  private var currentSize: Long = _

  def setId(value: String): Collector = {
    id = value
    this
  }

  def setTaskIdentifier(value: TaskIdentifier): Collector = {
    taskIdentifier = value
    this
  }

  def setSize(value: Long): Collector = {
    currentSize = value
    this
  }

  def start: Collector = {
    currentTime = System.currentTimeMillis
    if (startTime == -1L) startTime = currentTime
    currentSize = 0
    this
  }

  def stop: Collector = stop(true)

  def stop(taskEnd: Boolean): Collector = {
    endTime = System.currentTimeMillis
    totalTime += endTime - currentTime
    dataSize += currentSize
    if (taskEnd) taskNum += 1
    this
  }

  def isValid: Boolean = startTime != -1L && endTime != -1L
}
