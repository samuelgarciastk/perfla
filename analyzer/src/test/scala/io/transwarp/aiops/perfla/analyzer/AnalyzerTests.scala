package io.transwarp.aiops.perfla.analyzer

import org.junit.jupiter.api.Test

class AnalyzerTests {
  @Test
  def analyze(): Unit = {
    Analyzer.main(Array("-v", "/home/stk/Projects/PerfLA/loader/src/test/resources/fake_test.log"))
    // Analyzer.main(Array("-v", "/home/stk/Documents/perfla-test/bs4/e"))
  }
}
