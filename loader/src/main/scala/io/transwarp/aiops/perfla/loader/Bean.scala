package io.transwarp.aiops.perfla.loader

import scala.beans.BeanProperty

private[loader] class ConfigBean {
  @BeanProperty var settings: SettingBean = _
  @BeanProperty var tasks: Array[TaskBean] = _
}

private[loader] class SettingBean {
  @BeanProperty var logger_enable: Boolean = _
  @BeanProperty var watcher_enable: Boolean = _
  @BeanProperty var root_pwd: String = _
  @BeanProperty var prefix: String = _
  @BeanProperty var monitor_interval: Int = -1
  @BeanProperty var io_read: Double = -1
  @BeanProperty var io_write: Double = -1
  @BeanProperty var cpu: Double = -1
  @BeanProperty var cpu_idle: Double = -1
}

private[loader] class TaskBean {
  @BeanProperty var id: String = _
  @BeanProperty var clazz: String = _
  @BeanProperty var method: String = _
  @BeanProperty var pattern: String = _
  @BeanProperty var mode: String = _
  @BeanProperty var threshold: Array[ThresholdBean] = _
  @BeanProperty var sub_tasks: Array[String] = _
}

private[loader] class ThresholdBean {
  @BeanProperty var typ: String = _
  @BeanProperty var percent: Double = -1
  @BeanProperty var warn: Double = -1
  @BeanProperty var error: Double = -1
}
