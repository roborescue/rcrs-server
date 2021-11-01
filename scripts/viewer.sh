#!/bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <teamname> <hostname>"
  exit 1
fi

. functions.sh

TEAM_NAME=$1
HOSTNAME=$2

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP -Dlog4j.log.dir=$BASEDIR/logs/log rescuecore2.LaunchComponents sample.SampleViewer -c $BASEDIR/config/viewer.cfg -h $HOSTNAME --viewer.team-name=$TEAM_NAME