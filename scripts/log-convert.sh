#!/bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <log path> <target path with extension .7z .xz or .log>"
  exit 1
fi

. functions.sh

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx8056m -cp $CP sample.LogConvertor -c $BASEDIR/maps/test/config/logviewer.cfg $1 $2
