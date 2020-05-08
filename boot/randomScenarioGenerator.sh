#!/bin/bash
. functions.sh

echo "starting randomScenarioEditor..."

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP gis2.RandomScenarioGenerator $1 
