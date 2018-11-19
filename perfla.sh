#!/bin/bash

java -cp `find . -maxdepth 1 -name "perfla-analyzer-*.jar" -print -quit` io.transwarp.aiops.perfla.analyzer.Analyzer "$@"
