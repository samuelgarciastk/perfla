package io.transwarp.aiops.perfla.logger

import io.transwarp.aiops.perfla.loader.{Config, LogMod}
import org.junit.jupiter.api.{BeforeEach, Test}

class PerfLoggerTests {
  private var PLOG: PerfLogger = _

  private var collector: Collector = _

  @BeforeEach
  def init(): Unit = {
    PLOG = PerfLogger.getLogger
    collector = PLOG.collector("PerfLoggerTests", "fakeMethod")
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

  @Test
  def finiteLoop(): Unit = {
    for (_ <- 1 to 20) {
      println(Config.isValid)
      fakeMethod()
    }
    PLOG.log(collector, LogMod.FORCE)
  }

  private def fakeMethod(): Unit = {
    val checkpoint = PLOG.checkpoint(null).start
    collector.start
    Thread.sleep(1000)
    checkpoint.setSize(100)
    collector.setSize(100)
    PLOG.log(checkpoint, LogMod.FORCE)
    collector.stop
  }
}