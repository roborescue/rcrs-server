#!/bin/bash

. functions.sh

SCENARIO_DIR=$1

if [[ ! -z "$SCENARIO_DIR" ]]; then
  SCENARIO_DIR=$(abspath $SCENARIO_DIR)

  if [[ -z "$SCENARIO_DIR" ]]; then
    exit 1
  fi
fi

echo "starting scenarioEditor...."

makeClasspath $BASEDIR/jars $BASEDIR/lib
execute scenario-editor "java -Xmx512m -cp $CP -Dlog4j.log.dir=$BASEDIR/logs/log gis2.scenario.ScenarioEditor $SCENARIO_DIR"