#!/bin/bash

if [ ! $# -eq 3 ] ; then
    echo "usage: `basename $0` <teamname> <hostname> <dir>"
    exit 1
fi

. functions.sh

processArgs $*

TEAM_NAME=$1
HOSTNAME=$2
OUTDIR=$3

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.LiveLogExtractor -c $BASEDIR/config/viewer.cfg -h $HOSTNAME --viewer.team-name=$TEAM_NAME --viewer.output-dir=$OUTDIR
