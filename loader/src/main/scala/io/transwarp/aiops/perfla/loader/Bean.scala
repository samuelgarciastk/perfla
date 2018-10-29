package io.transwarp.aiops.perfla.loader

import scala.beans.BeanProperty

class ConfigBean {
  @BeanProperty var settings: SettingBean = _
  @BeanProperty var tasks: Array[TaskBean] = _
}

class SettingBean {
  @BeanProperty var prefix: String = _
  @BeanProperty var io_read: Double = _
  @BeanProperty var io_write: Double = _
  @BeanProperty var cpu: Double = _
}

class TaskBean {
  @BeanProperty var id: String = _
  @BeanProperty var class_name: String = _
  @BeanProperty var method_name: String = _
  @BeanProperty var pattern: String = _
  @BeanProperty var task_type: Array[String] = _
  @BeanProperty var warn_factor: Double = _
  @BeanProperty var error_factor: Double = _
}
