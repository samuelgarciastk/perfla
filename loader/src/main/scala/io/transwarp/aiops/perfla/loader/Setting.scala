package io.transwarp.aiops.perfla.loader

class Setting(settingBean: SettingBean) {
  val prefix: String = settingBean.prefix
  val io_read: Double = settingBean.io_read * 1000
  val io_write: Double = settingBean.io_write * 1000
  val cpu: Double = settingBean.cpu * 1000000
}
