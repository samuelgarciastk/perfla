package io.transwarp.aiops.perfla.analyzer.model

import io.transwarp.aiops.perfla.analyzer.{Analyzer, Utils}
import io.transwarp.aiops.perfla.loader.TaskLevel.TaskLevel
import io.transwarp.aiops.perfla.loader.{Config, Task, TaskLevel}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

private[analyzer] class TaskNode(taskId: String, parentNode: TaskNode) {
  val subTasks = new ArrayBuffer[TaskNode]
  val levelCount = new mutable.HashMap[TaskLevel, Int]
  var task: Task = _
  var startTime: Long = -1L
  var endTime: Long = -1L
  var time: Long = 0L
  var size: Long = 0L
  var num: Int = 0
  private var isEmpty = true

  {
    task = Config.taskIdMap(taskId)
    levelCount += TaskLevel.ERROR -> 0
    levelCount += TaskLevel.WARN -> 0
    levelCount += TaskLevel.INFO -> 0
    levelCount += TaskLevel.UNKNOWN -> 0
  }

  def merge(taskEntry: TaskEntry, fake: Boolean = false): TaskNode = if (!fake) {
    if (startTime == -1L) startTime = taskEntry.startTime
    if (endTime < taskEntry.endTime) endTime = taskEntry.endTime
    time += taskEntry.totalTime
    size += taskEntry.dataSize
    num += taskEntry.taskNum
    levelCount.update(taskEntry.level, levelCount(taskEntry.level) + 1)
    isEmpty = false
    this
  } else this

  def append(taskEntry: TaskEntry): TaskNode = {
    val targetId = taskEntry.taskId
    var targetNode: TaskNode = null
    var i = subTasks.length - 1
    var break = false

    // search sub nodes
    if (subTasks.nonEmpty) {
      targetNode = subTasks(i)
      while (!break) {
        if (targetNode canMerge taskEntry) {
          targetNode = targetNode merge taskEntry
          break = true
        } else if (targetNode canAppend taskEntry) {
          targetNode = targetNode append taskEntry
          break = true
        } else {
          i -= 1
          if (i < 0) break = true
          else targetNode = subTasks(i)
        }
      }
    }
    // create new node
    if (i < 0) {
      if (task directContains targetId) {
        val newSubTask = new TaskNode(targetId, this)
        subTasks += newSubTask
        targetNode = newSubTask merge taskEntry
      } else {
        assert(task.hasSubTasks)
        i = 0
        break = false
        val subTaskIdArray = task.subTaskIds.toArray
        var subTaskId = ""
        var subTask: Task = null
        while (!break && i < subTaskIdArray.length) {
          subTaskId = subTaskIdArray(i)
          subTask = Config.taskIdMap(subTaskId)

          if (subTask contains targetId) {
            val newSubTask = new TaskNode(subTaskId, this)
            subTasks += newSubTask
            newSubTask.merge(taskEntry, fake = true)
            targetNode = newSubTask append taskEntry
            break = true
          }
          i += 1
        }
        assert(i <= subTaskIdArray.length)
      }
    }
    targetNode
  }

  def print(prefix: String = ""): Unit = {
    if (isRoot) subTasks.foreach(subTask => {
      subTask.print()
      println()
    })
    else {
      println(prefix + this)
      // print sub tasks
      val basePrefix = if (prefix == "") ""
      else prefix.substring(0, prefix.length - 3) +
        (if (prefix.substring(prefix.length - 3) == "\\- ") "   " else "|  ")
      val taskPrefix = basePrefix + "+- "
      val lastPrefix = basePrefix + "\\- "
      var subTime = 0L

      if (isEmpty) {
        subTasks.init
        val lastIndex = subTasks.length - 1
        subTasks.zipWithIndex.foreach { case (subTask, index) =>
          subTime += subTask.time
          if (index == lastIndex) subTask.print(lastPrefix)
          else subTask.print(taskPrefix)
        }
      } else {
        subTasks.foreach(subTask => {
          subTime += subTask.time
          subTask.print(taskPrefix)
        })
        if (subTasks.nonEmpty) println(s"${lastPrefix}Unknown time: ${Utils.formatInterval(time - subTime)}")
      }
    }
  }

  override def toString: String = {
    val sb = new StringBuilder
    if (isEmpty) sb.append("[EMPTY] ").append(task.pattern)
    else {
      sb.append(task.pattern)
        .append(": ")
        .append(Utils.formatInterval(time))

      if (Analyzer.config.verbose)
        sb.append(" [")
          .append(Utils.formatTime(startTime))
          .append(" ~ ")
          .append(Utils.formatTime(endTime))
          .append("]")

      if (size != 0)
        sb.append(", ")
          .append(Utils.formatByte(size))

      sb.append(", ")
        .append(num)
        .append(" tasks")

      if (Analyzer.config.verbose) {
        sb.append(", [")
          .append(levelCount.filter(_._2 > 0).map { case (level, count) => s"$level: $count" }.mkString(", "))
          .append("]")
      }
    }
    sb.toString
  }

  def canAppend(taskEntry: TaskEntry): Boolean = isRoot ||
    (task contains taskEntry.taskId)

  //    ((task contains taskEntry.taskId) &&
  //      (isEmpty ||
  //        (startTime <= taskEntry.startTime &&
  //          taskEntry.endTime <= endTime)))

  def getParent: TaskNode = parentNode

  private def isRoot: Boolean = taskId == Task.root_id

  private def canMerge(taskEntry: TaskEntry): Boolean = taskEntry.taskId == taskId //&& taskEntry.startTime <= endTime
}
