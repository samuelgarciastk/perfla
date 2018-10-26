package io.transwarp.perfla.logger

import org.junit.jupiter.api.{BeforeEach, Test}

class PerfLoggerTests {
  private var PLOG: PerfLogger = _

  @BeforeEach
  def init: Unit = {
    PLOG = PerfLogger.getLogger
  }

  @Test
  def testCheckpoint: Unit = {
    fakeMethod
  }

  private def fakeMethod: Unit = {
    val checkpoint = PLOG.checkpoint
    Thread.sleep(10000)
    checkpoint.setDataSize(100)
    PLOG.log(checkpoint)
  }
}
