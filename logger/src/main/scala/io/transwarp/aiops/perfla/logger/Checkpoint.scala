package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, TaskIdentifier}

class Checkpoint {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var dataSize: Long = 0L
  private[logger] var startTime: Long = -1L
  private[logger] var endTime: Long = -1L

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
    if (Config.setting.loggerEnable) startTime = System.currentTimeMillis
    this
  }

  def stop: Checkpoint = {
    if (Config.setting.loggerEnable) endTime = System.currentTimeMillis
    this
  }

  def isValid: Boolean = startTime != -1L && endTime != -1L
}
