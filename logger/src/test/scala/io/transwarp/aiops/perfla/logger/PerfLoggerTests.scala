package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.Config
import org.junit.jupiter.api.{BeforeEach, Test}

class PerfLoggerTests {
  private var PLOG: PerfLogger = _

  @BeforeEach
  def init(): Unit = {
    PLOG = PerfLogger.getLogger
    PLOG.setMod(PerfLogMod.DEFAULT)
  }

  @Test
  def testDaemon(): Unit = {
    var i = 0
    while (i < 10) {
      println(Config.isValid)
      fakeMethod()
      i += 1
    }
    Config.stopWatchDaemon()
    Thread.sleep(1000)
    Config.startWatchDaemon()
    deadLoop()
  }

  @Test
  def deadLoop(): Unit = while (true) {
    println(Config.isValid)
    fakeMethod()
  }

  private def fakeMethod(): Unit = {
    val checkpoint = PLOG.checkpoint
    Thread.sleep(1000)
    checkpoint.setDataSize(100)
    PLOG.log(checkpoint)
  }
}