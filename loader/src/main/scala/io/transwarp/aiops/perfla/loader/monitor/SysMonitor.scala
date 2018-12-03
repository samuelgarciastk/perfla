package io.transwarp.aiops.perfla.loader.monitor

import java.util.{Timer, TimerTask}

import io.transwarp.aiops.perfla.loader.Config
import org.slf4j.LoggerFactory

import scala.collection.mutable.ArrayBuffer

trait SysMonitor {
  def run(): Unit
}

object SysMonitor {
  private val logger = LoggerFactory.getLogger(Config.getClass)
  private val monitors = new ArrayBuffer[SysMonitor]
  private var timer: Timer = _

  init()

  def start(): Unit = {
    if (timer != null) stop()
    timer = new Timer(true)
    timer.scheduleAtFixedRate(new TimerTask {
      override def run(): Unit = {
        monitors.foreach(_.run())
        // update threshold
        Config.taskIdMap.foreach { case (_, task) => if (task.threshold != null) task.threshold.load() }
      }
    }, Config.setting.monitorInterval, Config.setting.monitorInterval)
    logger.info("PerfLA-loader: Monitor started.")
  }

  def stop(): Unit = {
    timer.cancel()
    timer.purge()
    logger.info("PerfLA-loader: Monitor terminated.")
  }

  private def init(): Unit = {
    monitors += new IOReadMonitor
    monitors += new IOWriteMonitor
    monitors += new CPUMonitor
  }
}