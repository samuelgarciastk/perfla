package io.transwarp.aiops.perfla.analyzer

import java.io.File

import io.transwarp.aiops.perfla.analyzer.filter.{CountFilter, Filter, IdFilter}
import io.transwarp.aiops.perfla.analyzer.model.{TaskEntry, TaskTree}
import io.transwarp.aiops.perfla.loader.Config
import org.slf4j.LoggerFactory
import scopt.OptionParser

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

object Analyzer {
  private val logger = LoggerFactory.getLogger(Analyzer.getClass)
  private val parser = new OptionParser[CliConfig]("PerfLA") {
    head("PerfLA", "1.0.0")
    opt[Unit]('v', "verbose")
      .action((_, c) => c.copy(verbose = true))
      .text("show more information")
    opt[Unit]('h', "human")
      .action((_, c) => c.copy(humanReadable = true))
      .text("show human-readable output")
    opt[Int]('x', "max-count")
      .action((x, c) => c.copy(maxCount = x))
      .validate(x => if (x >= 0 && x <= Int.MaxValue) success else failure("Option --max-count must be >=0 and <= Integer.MAX_VALUE"))
      .text("maximum number of logging statements")
    opt[Int]('s', "min-count")
      .action((x, c) => c.copy(minCount = x))
      .validate(x => if (x >= 0 && x <= Int.MaxValue) success else failure("Option --min-count must be >=0 and <= Integer.MAX_VALUE"))
      .text("minimum number of logging statements")
    opt[Seq[String]]('i', "id")
      .valueName("<id1>,<id2>...")
      .action((x, c) => c.copy(ids = x))
      .text("specific ids to show")
    help("help").text("prints this usage text")
    arg[String]("<path>")
      .maxOccurs(1)
      .action((x, c) => c.copy(path = x))
      .text("file or directory path")
  }
  var config: CliConfig = _

  def main(args: Array[String]): Unit = {
    parser.parse(args, CliConfig()) match {
      case Some(c) =>
        config = c
        val filters = genFilters()
        analyze(config.path, filters)
      case None =>
    }
  }

  def analyze(pathStr: String, filters: ArrayBuffer[Filter]): Unit = if (Config.isValid) {
    Config.stopWatchDaemon()
    val path = new File(pathStr)
    if (path.exists) {
      val files = if (path.isDirectory) Utils.listFiles(path)
      else if (path.isFile) Array(path)
      else Array[File]()

      if (files.length != 0) {
        val analyzer = new Analyzer(files, filters)
        analyzer.work()
        analyzer.print()
      }
    } else logger.warn(s"PerfLA-analyzer: [$pathStr] not exist.")
  }

  def genFilters(): ArrayBuffer[Filter] = {
    val filters = new ArrayBuffer[Filter]
    if (config.ids != null) filters += new IdFilter(config.ids)
    if (config.maxCount != -1 || config.minCount != -1) filters += new CountFilter(config.minCount, config.maxCount)
    filters
  }
}

private[analyzer] class Analyzer(files: Array[File], filters: ArrayBuffer[Filter]) {
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
    val taskTree = new TaskTree(id)
    val sortedTaskEntries = taskEntries.sortBy(_.startTime)
    sortedTaskEntries.foreach(taskTree.append)
    treeMap += id -> taskTree
  }

  def print(): Unit = {
    val filteredMap =
      if (filters.nonEmpty)
        treeMap.filter { case (_, taskTree) => filters.map(_ satisfy taskTree).reduce(_ && _) }
      else
        treeMap
    filteredMap.foreach { case (id, taskTree) =>
      val delimiter = '+' + (-1 to id.length).map(_ => '-').mkString + '+'
      println(s"\n$delimiter\n| $id |\n$delimiter\nTotal logging statements: ${taskTree.logNum}\n")
      taskTree.print()
    }
  }
}
