package io.transwarp.aiops.perfla.loader.monitor

import io.transwarp.aiops.perfla.loader.{Config, Setting}

import scala.sys.process._

class IOReadMonitor extends SysMonitor {
  private val cmd = s"echo ${Config.setting.rootPWD}" #| "sudo -S hdparm -t /dev/sda"
  private val pattern = "(?<= = )[0-9.]+(?= MB)".r

  override def run(): Unit = {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    cmd ! ProcessLogger(stdout append _, stderr append _)

    pattern.findFirstIn(stdout).foreach(f => Config.setting.io_read = f.toDouble * Setting.io_factor)
  }
}
