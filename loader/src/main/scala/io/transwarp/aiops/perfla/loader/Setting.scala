package io.transwarp.aiops.perfla.loader

class Setting {
  var loggerEnable: Boolean = _
  var watcherEnable: Boolean = _
  var prefix: String = _
  var io_read: Double = _
  var io_write: Double = _
  var cpu: Double = _

  private[loader] def init(settingBean: SettingBean): Unit = {
    if (settingBean == null) {
      loggerEnable = Setting.default_logger_enable
      watcherEnable = Setting.default_watcher_enable
      prefix = Setting.default_prefix
      io_read = Setting.default_io_read * Setting.io_factor
      io_write = Setting.default_io_write * Setting.io_factor
      cpu = Setting.default_cpu * Setting.cpu_factor
    } else {
      loggerEnable = Option(settingBean.logger_enable).getOrElse(Setting.default_logger_enable)
      watcherEnable = Option(settingBean.watcher_enable).getOrElse(Setting.default_watcher_enable)
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

private object Setting {
  val default_logger_enable = false
  val default_watcher_enable = true
  val default_prefix = "[PerfLog]"
  val default_io_read = 300D
  val default_io_write = 30D
  val default_cpu = 2D
  val io_factor = 1000
  val cpu_factor = 1000000
}
