package io.transwarp.aiops.perfla.loader

import org.junit.jupiter.api.Test

class ConfigTests {
  @Test
  def config(): Unit = while (true) {
    println(Config.isValid)
    Thread.sleep(1000)
  }
}
