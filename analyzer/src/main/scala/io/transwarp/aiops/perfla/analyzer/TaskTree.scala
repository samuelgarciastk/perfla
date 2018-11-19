package io.transwarp.aiops.perfla.analyzer

import io.transwarp.aiops.perfla.loader.Task

private[analyzer] class TaskTree {
  private val root = new TaskNode(Task.root_id, null)
  var logNum = 0
  private var currentNode = root

  def append(taskEntry: TaskEntry): Unit = {
    logNum += 1
    while (!(currentNode canAppend taskEntry)) {
      currentNode = currentNode.getParent
    }
    currentNode = currentNode.append(taskEntry)
  }

  def print(): Unit = root.print()
}
