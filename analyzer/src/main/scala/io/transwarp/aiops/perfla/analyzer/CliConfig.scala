package io.transwarp.aiops.perfla.analyzer

case class CliConfig(
                      path: String = null,
                      verbose: Boolean = false,
                      humanReadable: Boolean = false,
                      ids: Seq[String] = null,
                      minCount: Int = -1,
                      maxCount: Int = -1
                    )
