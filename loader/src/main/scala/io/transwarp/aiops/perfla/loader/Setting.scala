package io.transwarp.aiops.perfla.loader

class Setting {
  var loggerEnable: Boolean = _
  var watcherEnable: Boolean = _
  var rootPWD: String = _
  var prefix: String = _
  var monitorInterval: Int = _
  var io_read: Double = _
  var io_write: Double = _
  var cpu: Double = _
  var cpu_idle: Double = _

  private[loader] def init(settingBean: SettingBean): Unit = {
    if (settingBean == null) {
      loggerEnable = Setting.default_logger_enable
      watcherEnable = Setting.default_watcher_enable
      rootPWD = Setting.default_root_pwd
      prefix = Setting.default_prefix
      monitorInterval = Setting.default_monitor_interval
      io_read = Setting.default_io_read * Setting.io_factor
      io_write = Setting.default_io_write * Setting.io_factor
      cpu = Setting.default_cpu * Setting.cpu_factor
      cpu_idle = Setting.default_cpu_idle
    } else {
      loggerEnable = Option(settingBean.logger_enable).getOrElse(Setting.default_logger_enable)
      watcherEnable = Option(settingBean.watcher_enable).getOrElse(Setting.default_watcher_enable)
      rootPWD = Option(settingBean.root_pwd).getOrElse(Setting.default_root_pwd)
      prefix = Option(settingBean.prefix).getOrElse(Setting.default_prefix)
      monitorInterval = if (settingBean.cpu_idle == -1) Setting.default_monitor_interval
      else settingBean.monitor_interval
      io_read = if (settingBean.io_read == -1) Setting.default_io_read * Setting.io_factor
      else settingBean.io_read * Setting.io_factor
      io_write = if (settingBean.io_write == -1) Setting.default_io_write * Setting.io_factor
      else settingBean.io_write * Setting.io_factor
      cpu = if (settingBean.cpu == -1) Setting.default_cpu * Setting.cpu_factor
      else settingBean.cpu * Setting.cpu_factor
      cpu_idle = if (settingBean.cpu_idle == -1) Setting.default_cpu_idle
      else settingBean.cpu_idle
    }
  }
}

private object Setting {
  val default_logger_enable = false
  val default_watcher_enable = true
  val default_root_pwd = "123456"
  val default_prefix = "[PerfLog]"
  val default_monitor_interval = 3600000
  val default_io_read = 300D
  val default_io_write = 30D
  val default_cpu = 2D
  val default_cpu_idle = 100D
  val io_factor = 1000
  val cpu_factor = 1000000
}
