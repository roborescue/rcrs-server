#!/bin/bash

. functions.sh

MAP_DIR=$1

if [[ ! -z "$MAP_DIR" ]]; then
  MAP_DIR=$(abspath $MAP_DIR)

  if [[ -z "$MAP_DIR" ]]; then
    exit 1
  fi
fi

echo "starting randomScenarioGenerator..."

makeClasspath $BASEDIR/jars $BASEDIR/lib
execute random-scenario-editor "java -Xmx512m -cp $CP -Dlog4j.log.dir=$BASEDIR/logs/log gis2.scenario.RandomScenarioGenerator $MAP_DIR $*"