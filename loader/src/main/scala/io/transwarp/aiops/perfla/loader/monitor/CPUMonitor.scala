package io.transwarp.aiops.perfla.loader.monitor

import io.transwarp.aiops.perfla.loader.Config

import scala.sys.process._

class CPUMonitor extends SysMonitor {
  private val cmd = "iostat"
  private val pattern = "(?<=%idle)[0-9.\\s]+(?=Device)".r

  override def run(): Unit = {
    val result = cmd !!

    pattern.findFirstIn(result).foreach(f => {
      val elems = f.trim.split("\\s+")
      if (elems.length > 5) Config.setting.cpu_idle = elems(5).toDouble
    })
  }
}
