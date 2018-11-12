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

  def listFiles(dir: File): Array[File] = {
    val files = dir.listFiles
    files.filter(_.isFile) ++ files.filter(_.isDirectory).flatMap(listFiles)
  }

  def formatTime(t: Long): String = {
    var sec = t / 1000
    if (sec == 0) return s"${t}ms"
    val millis = t % 1000
    var min = sec / 60
    if (min == 0) return s"${sec}s ${millis}ms"
    sec = sec % 60
    val hour = min / 60
    if (hour == 0) return s"${min}m ${sec}s ${millis}ms"
    min = min % 60
    s"${hour}h ${min}m ${sec}s ${millis}ms"
  }

  def formatByte(b: Long): String = {
    val unit = 1024
    if (b < unit) s"$b B"
    else {
      val exp = (Math.log(b) / Math.log(unit)).toInt
      val pre = "KMGTPE".charAt(exp - 1) + "iB"
      val res = b / Math.pow(unit, exp)
      "%.1f %s".format(res, pre)
    }
  }
}

class Analyzer(files: Array[File]) {
  type idMap = mutable.HashMap[String, TaskNode]
  private val sqlMap = new mutable.HashMap[String, idMap]

  def work(): Unit = {
    files.foreach(file => {
      val source = Source.fromFile(file)
      val lines = source.getLines

      lines.foreach(line => if (line.indexOf(Config.setting.prefix) != -1) {
        val taskEntry = new TaskEntry(line)
        if (taskEntry.task != null) {
          if (sqlMap.contains(taskEntry.id)) {
            val idMap = sqlMap(taskEntry.id)
            if (idMap.contains(taskEntry.task.id)) {
              val taskNode = idMap(taskEntry.task.id)
              taskNode.time += taskEntry.totalTime
              taskNode.size += taskEntry.dataSize
              taskNode.num += taskEntry.taskNum
            } else {
              val taskNode = new TaskNode(taskEntry.task.id, taskEntry.task.pattern, taskEntry.task.subTasks)
              taskNode.time += taskEntry.totalTime
              taskNode.size += taskEntry.dataSize
              taskNode.num += taskEntry.taskNum

              idMap += taskEntry.task.id -> taskNode
            }
          } else {
            val taskNode = new TaskNode(taskEntry.task.id, taskEntry.task.pattern, taskEntry.task.subTasks)
            taskNode.time += taskEntry.totalTime
            taskNode.size += taskEntry.dataSize
            taskNode.num += taskEntry.taskNum

            val idMap = new mutable.HashMap[String, TaskNode]
            idMap += taskEntry.task.id -> taskNode
            sqlMap += taskEntry.id -> idMap
          }
        }
      })

      source.close
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
  }

  def print(): Unit = {
    sqlMap.foreach { case (sql, idMap) =>
      val delimiter = '+' + (-1 to sql.length).map(_ => '-').mkString + '+'
      println(s"\n$delimiter\n| $sql |\n$delimiter\n")
      idMap.foreach(_._2.print(""))
    }
  }
}
