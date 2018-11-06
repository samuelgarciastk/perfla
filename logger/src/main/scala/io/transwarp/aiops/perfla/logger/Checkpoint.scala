package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.TaskIdentifier

class Checkpoint {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var dataSize: Long = 1L
  private[logger] var startTime: Long = -1L
  private[logger] var endTime: Long = -1L
  private[logger] var interval: Long = -1L

  def setId(value: String): Checkpoint = {
    id = value
    this
  }

  def setTaskIdentifier(value: TaskIdentifier): Checkpoint = {
    taskIdentifier = value
    this
  }

  def setSize(value: Long): Checkpoint = {
    dataSize = value
    this
  }

  def start: Checkpoint = {
    startTime = System.currentTimeMillis
    this
  }

  def stop: Checkpoint = {
    endTime = System.currentTimeMillis
    interval = endTime - startTime
    this
  }

  def isValid: Boolean = interval != -1L
}
