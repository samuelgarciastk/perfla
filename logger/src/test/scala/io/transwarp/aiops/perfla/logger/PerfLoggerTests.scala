package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, LogMod}
import org.junit.jupiter.api.{BeforeEach, Test}

class PerfLoggerTests {
  private var PLOG: PerfLogger = _

  private var checkpoint: PerfCheckpoint = _

  @BeforeEach
  def init(): Unit = {
    PLOG = PerfLogger.getLogger
    checkpoint = PLOG.checkpoint("PerfLoggerTests", "fakeMethod")
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
    checkpoint.start
    Thread.sleep(1000)
    checkpoint.setSize(100)
    checkpoint.stop
  }

  @Test
  def finiteLoop(): Unit = {
    for (_ <- 1 to 20) {
      println(Config.isValid)
      fakeMethod()
    }
    PLOG.log(checkpoint, LogMod.FORCE)
  }
}