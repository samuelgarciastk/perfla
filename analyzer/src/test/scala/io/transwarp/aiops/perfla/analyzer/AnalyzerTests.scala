package io.transwarp.aiops.perfla.analyzer

import java.io.File

import org.junit.jupiter.api.Test

class AnalyzerTests {
  @Test
  def analyze: Unit = {
    val file = new File("/home/stk/Downloads/perflog.log")
    val analyzer = new Analyzer(file)
    analyzer.work
    analyzer.printStatistics
  }
}
