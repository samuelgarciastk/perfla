package io.transwarp.aiops.perfla.loader

import io.transwarp.aiops.perfla.loader.ThresholdType.ThresholdType

class Threshold {
  var warn: Double = _
  var error: Double = _

  def init(thresholds: Array[ThresholdBean]): Unit = {
    if (thresholds == null || thresholds.length == 0) {
      warn = 0
      error = 0
    } else {
      thresholds.foreach(threshold => {
        normalize(threshold) match {
          case ThresholdType.IO_R =>
            warn += threshold.warn / Config.setting.io_read * threshold.percent
            error += threshold.error / Config.setting.io_read * threshold.percent
          case ThresholdType.IO_W =>
            warn += threshold.warn / Config.setting.io_write * threshold.percent
            error += threshold.error / Config.setting.io_write * threshold.percent
          case ThresholdType.CPU =>
            warn += threshold.warn / Config.setting.cpu * threshold.percent
            error += threshold.error / Config.setting.cpu * threshold.percent
          case ThresholdType.MEM =>
          case _ =>
        }
      })
    }
  }

  private def normalize(thresholdBean: ThresholdBean): ThresholdType = {
    if (thresholdBean.percent == -1) thresholdBean.percent = Threshold.default_percent
    if (thresholdBean.warn == -1) thresholdBean.warn = Threshold.default_warn
    if (thresholdBean.error == -1) thresholdBean.error = Threshold.default_error
    ThresholdType.withNameWithDefault(thresholdBean.typ)
  }
}

private object Threshold {
  val default_percent = 1D
  val default_warn = 5D
  val default_error = 10D
}

object ThresholdType extends Enumeration {
  type ThresholdType = Value
  val UNKNOWN, IO_R, IO_W, CPU, MEM = Value

  def withNameWithDefault(name: String): Value = values.find(_.toString == name).getOrElse(UNKNOWN)
}
