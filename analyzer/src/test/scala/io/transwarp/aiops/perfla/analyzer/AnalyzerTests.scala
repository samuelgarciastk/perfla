package io.transwarp.aiops.perfla.analyzer

import org.junit.jupiter.api.Test

class AnalyzerTests {
  @Test
  def analyze(): Unit = {
    Analyzer.main(Array("-v", "/home/stk/Projects/PerfLA/loader/src/test/resources/fake_test.log"))
  }
}
