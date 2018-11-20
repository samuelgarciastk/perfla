package io.transwarp.aiops.perfla.analyzer.filter

import io.transwarp.aiops.perfla.analyzer.model.TaskTree

class IdFilter(ids: Seq[String]) extends Filter {
  override def satisfy(taskTree: TaskTree): Boolean = ids contains taskTree.id
}
