package io.transwarp.aiops.perfla.analyzer

import java.io.{File, FileWriter}

import io.transwarp.aiops.perfla.loader.Config
import org.junit.jupiter.api.Test

import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class AnalyzerTests {
  @Test
  def analyze(): Unit = {
    Analyzer.main(Array("-vh", "/home/stk/Projects/PerfLA/loader/src/test/resources/bs5_test.log"))
    //Analyzer.main(Array("-vh", "/home/stk/Projects/PerfLA/loader/src/test/resources/fake_test.log"))
    //Analyzer.main(Array("-vh", "/home/stk/Documents/perfla-test/bs5/f"))
  }

  @Test
  def genTestFile(): Unit = {
    val inPath = "/home/stk/Documents/perfla-test/bs5/f"
    val outPath = "/home/stk/Projects/PerfLA/loader/src/test/resources/bs5_test.log"

    val entries = new ArrayBuffer[TaskEntry]
    val path = new File(inPath)
    if (path.exists) {
      val files = if (path.isDirectory) Utils.listFiles(path)
      else if (path.isFile) Array(path)
      else Array[File]()

      if (files.length != 0) {
        files.foreach(file => {
          val source = Source.fromFile(file)
          val lines = source.getLines
          lines.foreach(line => if (line.contains(Config.setting.prefix)) {
            entries += new TaskEntry(line)
          })
          source.close
        })
      }

      val sortedEntries = entries.sortBy(_.startTime)

      val outFile = new File(outPath)
      val writer = new FileWriter(outFile)
      sortedEntries.foreach(line => {
        writer.write(line.line)
        writer.write('\n')
      })
      writer.flush()
      writer.close()
    }
  }
}
