package io.transwarp.aiops.perfla.analyzer.filter

import io.transwarp.aiops.perfla.analyzer.model.TaskTree

class CountFilter(min: Int, max: Int) extends Filter {
  override def satisfy(taskTree: TaskTree): Boolean =
    (min == -1 || min <= taskTree.logNum) &&
      (max == -1 || taskTree.logNum <= max)
}
