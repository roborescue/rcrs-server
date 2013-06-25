#! /bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <logfile> <outdir>"
  exit 1
fi

. functions.sh

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx1024m -cp $CP -Dlog4j.log.dir=logs rescuecore2.log.LogExtractor -c config/logviewer.cfg $1 $2
