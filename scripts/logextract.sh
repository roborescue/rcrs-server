#!/bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <rescue.log path> <output dir>"
  exit 1
fi

. functions.sh

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx3024m -cp $CP -Dlog4j.log.dir=$BASEDIR/logs/log rescuecore2.log.LogExtractor -c $BASEDIR/maps/kobe/config/logviewer.cfg -c $BASEDIR/maps/kobe/config/kernel.cfg $1 $2
