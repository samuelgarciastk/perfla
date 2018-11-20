package io.transwarp.aiops.perfla.analyzer.filter

import io.transwarp.aiops.perfla.analyzer.model.TaskTree

trait Filter {
  def satisfy(taskTree: TaskTree): Boolean
}
