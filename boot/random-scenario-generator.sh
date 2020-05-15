#!/bin/bash

cd `dirname $0`

. utils.sh
. functions.sh

LOGDIR='logs'
SCENARIO_DIR=$1

if [[ ! -z "$SCENARIO_DIR" ]]; then
    SCENARIO_DIR=$(abspath $SCENARIO_DIR)

    if [[ -z "$SCENARIO_DIR" ]]; then
        exit 1
    fi
fi

echo "starting randomScenarioGenerator..."

makeClasspath $BASEDIR/jars $BASEDIR/lib
execute random-scenario-editor "java -Xmx512m -cp $CP -Dlog4j.log.dir=$LOGDIR gis2.RandomScenarioGenerator $SCENARIO_DIR"
