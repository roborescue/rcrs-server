#! /bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <teamname> <hostname>"
  exit 1
fi

TEAM_NAME=$1
HOSTNAME=$2

. functions.sh

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleViewer -c config/viewer.cfg -h $HOSTNAME --viewer.team-name=$TEAM_NAME 
