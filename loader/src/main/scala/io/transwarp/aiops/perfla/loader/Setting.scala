package io.transwarp.aiops.perfla.loader

class Setting(settingBean: SettingBean) {
  if (settingBean == null) throw new IllegalArgumentException("PerfLA-loader: 'settings' field not found.")

  val prefix: String = Option(settingBean.prefix).getOrElse("[PerfLog]")
  val io_read: Double = Option(settingBean.io_read).getOrElse(300D) * 1000
  val io_write: Double = Option(settingBean.io_write).getOrElse(30D) * 1000
  val cpu: Double = Option(settingBean.cpu).getOrElse(2D) * 1000000
}
