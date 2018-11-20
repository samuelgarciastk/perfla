package io.transwarp.aiops.perfla.analyzer.model

import io.transwarp.aiops.perfla.loader.Task

private[analyzer] class TaskTree(val id: String) {
  private val root = new TaskNode(Task.root_id, null)
  var logNum = 0
  private var currentNode = root

  @Deprecated
  def appendOldVersion(taskEntry: TaskEntry): Unit = {
    logNum += 1
    while (!(currentNode canAppend taskEntry)) {
      currentNode = currentNode.getParent
    }
    currentNode = currentNode.append(taskEntry)
  }

  def append(taskEntry: TaskEntry): Unit = {
    logNum += 1
    currentNode.append(taskEntry)
  }

  def print(): Unit = root.print()
}
