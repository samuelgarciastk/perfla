#!/bin/sh

mvn clean install

dir='/home/stk/Documents/perfla'
cp logger/target/perfla-logger-1.0.0-SNAPSHOT.jar "$dir/jars"
cp analyzer/target/perfla-analyzer-1.0.0-SNAPSHOT.jar "$dir"