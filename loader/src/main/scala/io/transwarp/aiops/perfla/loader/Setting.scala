package io.transwarp.aiops.perfla.loader

class Setting {
  var prefix: String = _
  var io_read: Double = _
  var io_write: Double = _
  var cpu: Double = _

  def init(settingBean: SettingBean): Unit = {
    if (settingBean == null) {
      prefix = Setting.default_prefix
      io_read = Setting.default_io_read * Setting.io_factor
      io_write = Setting.default_io_write * Setting.io_factor
      cpu = Setting.default_cpu * Setting.cpu_factor
    } else {
      prefix = Option(settingBean.prefix).getOrElse(Setting.default_prefix)
      io_read = if (settingBean.io_read == -1) Setting.default_io_read * Setting.io_factor
      else settingBean.io_read * Setting.io_factor
      io_write = if (settingBean.io_write == -1) Setting.default_io_write * Setting.io_factor
      else settingBean.io_write * Setting.io_factor
      cpu = if (settingBean.cpu == -1) Setting.default_cpu * Setting.cpu_factor
      else settingBean.cpu * Setting.cpu_factor
    }
  }
}

object Setting {
  private val default_prefix = "[PerfLog]"
  private val default_io_read = 300D
  private val default_io_write = 30D
  private val default_cpu = 2D
  private val io_factor = 1000
  private val cpu_factor = 1000000
}
