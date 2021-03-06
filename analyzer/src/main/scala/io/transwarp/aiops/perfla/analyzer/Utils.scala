package io.transwarp.aiops.perfla.analyzer

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

private[analyzer] object Utils {
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")

  def listFiles(dir: File): Array[File] = {
    val files = dir.listFiles
    files.filter(_.isFile) ++ files.filter(_.isDirectory).flatMap(listFiles)
  }

  def formatInterval(t: Long, unit: String): String = unit match {
    case "ms" => formatMillisInterval(t)
    case "ns" => formatNanoInterval(t)
    case _ => t.toString
  }

  private def formatMillisInterval(t: Long): String = if (Analyzer.config.humanReadable) {
    var sec = t / 1000
    if (sec == 0) return s"${t}ms"
    val millis = t % 1000
    var min = sec / 60
    if (min == 0) return s"${sec}s ${millis}ms"
    sec = sec % 60
    val hour = min / 60
    if (hour == 0) return s"${min}m ${sec}s ${millis}ms"
    min = min % 60
    s"${hour}h ${min}m ${sec}s ${millis}ms"
  } else s"${t}ms"

  private def formatNanoInterval(t: Long): String = if (Analyzer.config.humanReadable) {
    var millis = t / 1000000
    if (millis == 0) return s"${t}ns"

    val nanos = t % 1000
    var sec = millis / 1000
    if (sec == 0) return s"${millis}ms ${nanos}ns"

    millis = t % 1000
    var min = sec / 60
    if (min == 0) return s"${sec}s ${millis}ms ${nanos}ns"

    sec = sec % 60
    val hour = min / 60
    if (hour == 0) return s"${min}m ${sec}s ${millis}ms ${nanos}ns"

    min = min % 60
    s"${hour}h ${min}m ${sec}s ${millis}ms ${nanos}ns"
  } else s"${t}ns"

  def formatByte(b: Long): String = if (Analyzer.config.humanReadable) {
    val unit = 1024
    if (b < unit) s"$b B"
    else {
      val exp = (Math.log(b) / Math.log(unit)).toInt
      val pre = "KMGTPE".charAt(exp - 1) + "iB"
      val res = b / Math.pow(unit, exp)
      "%.1f %s".format(res, pre)
    }
  } else s"$b B"

  def formatTime(t: Long): String = if (Analyzer.config.humanReadable) sdf.format(new Date(t)) else t.toString
}
