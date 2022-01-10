#!/bin/bash

if [ ! $# -eq 1 ] ; then
  echo "usage: `basename $0` <rescue.log path>"
  exit 1
fi

. functions.sh

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx256m -cp $CP sample.SampleLogViewer -c $BASEDIR/config/logviewer.cfg $1