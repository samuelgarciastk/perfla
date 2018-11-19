package io.transwarp.aiops.perfla.analyzer

import java.io.File

import io.transwarp.aiops.perfla.loader.Config
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object Analyzer {
  private val logger = LoggerFactory.getLogger(Analyzer.getClass)
  private val usage =
    """Usage: perfla [<options>] <file or directory path>
      |
      |Options:
      |-v, --verbose  show more information
      |-h, --human    show human-readable output
      |    --help     display this help and exit""".stripMargin
  private[analyzer] var verbose = false
  private[analyzer] var humanReadable = false

  def main(args: Array[String]): Unit = {
    var path: String = ""

    def exit(): Unit = {
      println(usage)
      System.exit(1)
    }

    def parseOption(list: List[String]): Unit = {
      list match {
        case Nil => exit()
        case ("-v" | "--verbose") :: tail =>
          verbose = true
          parseOption(tail)
        case ("-h" | "--human") :: tail =>
          humanReadable = true
          parseOption(tail)
        case ("-vh" | "-hv") :: tail =>
          verbose = true
          humanReadable = true
          parseOption(tail)
        case "--help" :: _ => exit()
        case option :: _ if option == "" || option.head == '-' => exit()
        case pathStr :: Nil => path = pathStr
        case _ :: _ => exit()
      }
    }

    parseOption(args.toList)
    analyze(path)
  }

  def analyze(pathStr: String): Unit = if (Config.isValid) {
    Config.stopWatchDaemon()
    val path = new File(pathStr)
    if (path.exists) {
      val files = if (path.isDirectory) Utils.listFiles(path)
      else if (path.isFile) Array(path)
      else Array[File]()

      if (files.length != 0) {
        val analyzer = new Analyzer(files)
        analyzer.work()
        analyzer.print()
      }
    } else logger.warn(s"PerfLA-analyzer: [$pathStr] not exist.")
  }
}

private[analyzer] class Analyzer(files: Array[File]) {
  private val taskMap = new mutable.HashMap[String, ArrayBuffer[TaskEntry]]
  private val treeMap = new mutable.HashMap[String, TaskTree]

  def work(): Unit = {
    groupTaskEntry()
    genTrees()
  }

  private def groupTaskEntry(): Unit = files.foreach(file => {
    val source = Source.fromFile(file)
    val lines = source.getLines

    lines.foreach(line => if (line contains Config.setting.prefix) {
      val taskEntry = new TaskEntry(line)
      if (Config.taskIdMap.contains(taskEntry.taskId)) {
        if (taskMap.contains(taskEntry.id)) {
          taskMap(taskEntry.id) += taskEntry
        } else {
          val taskEntries = new ArrayBuffer[TaskEntry]
          taskEntries += taskEntry
          taskMap += taskEntry.id -> taskEntries
        }
      }
    })

    source.close
  })

  private def genTrees(): Unit = taskMap.foreach { case (id, taskEntries) =>
    val taskTree = new TaskTree
    val sortedTaskEntries = taskEntries.sortBy(_.startTime)
    sortedTaskEntries.foreach(taskTree.append)
    treeMap += id -> taskTree
  }

  def print(): Unit = {
    treeMap.foreach { case (id, taskTree) =>
      val delimiter = '+' + (-1 to id.length).map(_ => '-').mkString + '+'
      println(s"\n$delimiter\n| $id |\n$delimiter\nTotal logging statements: ${taskTree.logNum}\n")
      taskTree.print()
    }
  }
}
