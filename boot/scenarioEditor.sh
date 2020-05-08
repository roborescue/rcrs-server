#!/bin/bash
. functions.sh



echo "starting scenarioEditor...."

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP gis2.scenario.ScenarioEditor $1
