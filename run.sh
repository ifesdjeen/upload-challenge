#!/bin/bash

UPLOADCHALLENGE_HOME="/a/uploadchallenge/current"
UPLOADCHALLENGE_JAR="target/uploadchallenge-0.1.0-SNAPSHOT-standalone.jar"

java -Xmx1024m -jar $UPLOADCHALLENGE_HOME/$UPLOADCHALLENGE_JAR --config $UPLOADCHALLENGE_HOME/config/production.clj &
echo $! > $UPLOADCHALLENGE_HOME/tmp/pids/uploadchallenge.pid
exit 0

