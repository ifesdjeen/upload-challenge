#!/bin/bash

UPLOADCHALLENGE_HOME="/a/uploadchallenge/current"
UPLOADCHALLENGE_JAR="target/uploadchallenge-1.0.0-SNAPSHOT-standalone.jar"

java -Xmx1024m -jar $UPLOADCHALLENGE_HOME/$UPLOADCHALLENGE_JAR --config-file $UPLOADCHALLENGE_HOME/config/production.clj --environment production &
echo $! > $UPLOADCHALLENGE_HOME/tmp/pids/uploadchallenge.pid
exit 0

