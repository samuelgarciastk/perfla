package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, Task, TaskIdentifier}

class Checkpoint {
  var id: String = _
  var dataSize: Long = 1
  private[logger] var startTime: Long = _
  private[logger] var endTime: Long = _
  private[logger] var interval: Long = _
  private[logger] var task: Task = _
  private var taskIdentifier: TaskIdentifier = _
  private var valid = false

  def isValid: Boolean = valid

  def start: Checkpoint = {
    valid = false
    startTime = System.currentTimeMillis
    this
  }

  def stop: Checkpoint = {
    endTime = System.currentTimeMillis
    interval = endTime - startTime
    valid = true
    this
  }

  def initTask: Task = {
    if (taskIdentifier != null && Config.isValid)
      task = Config.identifierMap.get(taskIdentifier).orNull
    task
  }

  def setId(value: String): Checkpoint = {
    id = value
    this
  }

  def setTaskIdentifier(value: TaskIdentifier): Checkpoint = {
    taskIdentifier = value
    this
  }

  def setDataSize(value: Long): Checkpoint = {
    dataSize = value
    this
  }
}
