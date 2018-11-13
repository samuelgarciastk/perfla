package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.TaskIdentifier

class Checkpoint {
  private[logger] var id: String = _
  private[logger] var taskIdentifier: TaskIdentifier = _
  private[logger] var dataSize: Long = 0L
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

  /**
    * Set data size.
    * @param value data size
    * @return
    */
  def setSize(value: Long): Checkpoint = {
    dataSize = value
    this
  }

  /**
    * Start timer.
    * @return
    */
  def start: Checkpoint = {
    startTime = System.currentTimeMillis
    this
  }

  /**
    * Stop timer.
    * @return
    */
  def stop: Checkpoint = {
    endTime = System.currentTimeMillis
    interval = endTime - startTime
    this
  }

  /**
    * Check whether this checkpoint is ready to be record.
    * @return
    */
  def isValid: Boolean = interval != -1L
}
