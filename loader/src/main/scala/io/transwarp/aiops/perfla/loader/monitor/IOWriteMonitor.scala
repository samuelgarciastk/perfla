package io.transwarp.aiops.perfla.loader.monitor

import io.transwarp.aiops.perfla.loader.{Config, Setting}

import scala.sys.process._

class IOWriteMonitor extends SysMonitor {
  private val cmd = "dd if=/dev/zero of=/tmp/iotest bs=8k count=10000 conv=fdatasync"
  private val pattern = ", ([0-9.]+) (MB|GB|KB)".r

  override def run(): Unit = {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    cmd ! ProcessLogger(stdout append _, stderr append _)

    pattern.findFirstMatchIn(stderr).foreach(f => f.group(2) match {
      case "MB" => Config.setting.io_write = f.group(1).toDouble * Setting.io_factor
      case "GB" => Config.setting.io_write = f.group(1).toDouble * Setting.io_factor * 1000
      case "KB" => Config.setting.io_write = f.group(1).toDouble * Setting.io_factor / 1000
      case "B" => Config.setting.io_write = f.group(1).toDouble * Setting.io_factor / 1000000
      case _ =>
    })
  }
}
