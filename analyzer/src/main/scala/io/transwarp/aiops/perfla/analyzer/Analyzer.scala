package io.transwarp.aiops.perfla.analyzer

import java.io.File

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class Analyzer(file: File) {
  type patternMap = mutable.HashMap[String, ArrayBuffer[TaskEntry]]
  private val uuidMap = new mutable.HashMap[String, patternMap]
  private val statisticsMap = new mutable.HashMap[String, mutable.HashMap[String, TaskStatistics]]

  def work: Unit = {
    val source = Source.fromFile(file)
    val lines = source.getLines
    lines.foreach(line => {
      val taskEntry = new TaskEntry(line)
      if (uuidMap.contains(taskEntry.uuid)) {
        if (taskEntry.task != null) {
          val patternMap = uuidMap(taskEntry.uuid)
          val patternStatistics = statisticsMap(taskEntry.uuid)

          if (patternMap.contains(taskEntry.task.pattern)) {
            val list = patternMap(taskEntry.task.pattern)
            list += taskEntry

            val statistics = patternStatistics(taskEntry.task.pattern)
            statistics.time += taskEntry.diff
            statistics.size += taskEntry.dataSize
          } else {
            val list = new ArrayBuffer[TaskEntry]
            list += taskEntry
            patternMap += taskEntry.task.pattern -> list

            patternStatistics += taskEntry.task.pattern -> TaskStatistics(taskEntry.diff, taskEntry.dataSize)
          }
        }
      } else {
        val list = new ArrayBuffer[TaskEntry]
        list += taskEntry
        val patternMap = new mutable.HashMap[String, ArrayBuffer[TaskEntry]]
        patternMap += taskEntry.task.pattern -> list
        uuidMap += taskEntry.uuid -> patternMap

        val patternStatistics = new mutable.HashMap[String, TaskStatistics]
        patternStatistics += taskEntry.task.pattern -> TaskStatistics(taskEntry.diff, taskEntry.dataSize)
        statisticsMap += taskEntry.uuid -> patternStatistics
      }
    })
    source.close
  }

  def print: Unit = {
    uuidMap.foreach { case (uuid, patternMap) =>
      println(s"=====$uuid=====")
      patternMap.foreach { case (pattern, taskEntries) =>
        println(s"\t$pattern:")
        taskEntries.foreach(taskEntry => {
          println(s"\t\t${taskEntry.startTime} ${taskEntry.endTime} ${taskEntry.dataSize} ${taskEntry.level}")
        })
      }
    }
  }

  def printStatistics: Unit = {
    statisticsMap.foreach { case (uuid, patternMap) =>
      println(s"=====$uuid=====")
      patternMap.foreach { case (pattern, task) =>
        println(s"\t$pattern: ${task.time}ms ${task.size}Byte")
      }
    }
  }
}

case class TaskStatistics(var time: Long, var size: Long)
