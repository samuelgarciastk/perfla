package io.transwarp.aiops.perfla.analyzer

import java.io.File

import io.transwarp.aiops.perfla.loader.Config
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.io.Source

object Analyzer {
  private val logger = LoggerFactory.getLogger(Analyzer.getClass)

  def main(args: Array[String]): Unit = {
    if (args.length != 1) throw new IllegalArgumentException("PerfLA-analyzer: Malformed args.")
    Config.stopWatchDaemon()
    if (Config.isValid) {
      val pathStr = args.head
      val path = new File(pathStr)
      if (path.exists) {
        val files = if (path.isDirectory) listFiles(path)
        else if (path.isFile) Array(path)
        else Array[File]()
        if (files.length != 0) {
          val analyzer = new Analyzer(files)
          analyzer.work()
          analyzer.print()
        }
      } else {
        logger.warn(s"PerfLA-analyzer: [$pathStr] not exist.")
      }
    }
  }

  private def listFiles(dir: File): Array[File] = {
    val files = dir.listFiles
    files.filter(_.isFile) ++ files.filter(_.isDirectory).flatMap(listFiles)
  }
}

class Analyzer(files: Array[File]) {
  type idMap = mutable.HashMap[String, TaskNode]
  private val sqlMap = new mutable.HashMap[String, idMap]

  def work(): Unit = files.foreach(file => {
    val source = Source.fromFile(file)
    val lines = source.getLines

    lines.foreach(line => if (line.indexOf(Config.setting.prefix) != -1) {
      val taskEntry = new TaskEntry(line)
      if (taskEntry.task != null) {
        if (sqlMap.contains(taskEntry.id)) {
          val idMap = sqlMap(taskEntry.id)
          if (idMap.contains(taskEntry.task.id)) {
            val taskNode = idMap(taskEntry.task.id)
            taskNode.time += taskEntry.diff
            taskNode.size += taskEntry.dataSize
            taskNode.num += 1
          } else {
            val taskNode = new TaskNode(taskEntry.task.id, taskEntry.task.pattern, taskEntry.task.subTasks)
            taskNode.time += taskEntry.diff
            taskNode.size += taskEntry.dataSize
            taskNode.num += 1

            idMap += taskEntry.task.id -> taskNode
          }
        } else {
          val taskNode = new TaskNode(taskEntry.task.id, taskEntry.task.pattern, taskEntry.task.subTasks)
          taskNode.time += taskEntry.diff
          taskNode.size += taskEntry.dataSize
          taskNode.num += 1

          val idMap = new mutable.HashMap[String, TaskNode]
          idMap += taskEntry.task.id -> taskNode
          sqlMap += taskEntry.id -> idMap
        }
      }
    })

    sqlMap.foreach { case (sql, idMap) =>
      idMap.foreach { case (_, taskNode) =>
        if (taskNode.subTasksId != null) {
          taskNode.subTasksId.foreach(id => {
            idMap.get(id).foreach(subTaskNode => {
              taskNode.subTasks += subTaskNode
              subTaskNode.flag = false
            })
          })
        }
      }
      sqlMap += sql -> idMap.filter(_._2.flag)
    }

    source.close
  })

  def print(): Unit = {
    sqlMap.foreach { case (sql, idMap) =>
      println(s"=====$sql=====")
      idMap.foreach(_._2.print(""))
    }
  }
}
