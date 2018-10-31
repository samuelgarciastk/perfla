package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.Config
import org.junit.jupiter.api.{BeforeEach, Test}

class PerfLoggerTests {
  private var PLOG: PerfLogger = _

  @BeforeEach
  def init: Unit = {
    PLOG = PerfLogger.getLogger
    PLOG.setMod(PerfLogMod.DEFAULT)
  }

  @Test
  def testCheckpoint: Unit = {
    while (true) {
      println(Config.isValid)
      fakeMethod
    }
  }

  private def fakeMethod: Unit = {
    val checkpoint = PLOG.checkpoint
    Thread.sleep(1000)
    checkpoint.setDataSize(100)
    PLOG.log(checkpoint)
  }
}