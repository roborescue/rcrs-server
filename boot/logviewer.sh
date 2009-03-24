#! /bin/sh

if [ $# -eq 0 ] ; then
  echo "usage: `basename $0` <logfile>"
  exit 1
fi

DIR=`dirname $0`
java -Xmx128m -cp $DIR/../programs/viewer/ viewer.Main -l $*
