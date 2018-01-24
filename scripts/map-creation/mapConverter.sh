#!/bin/bash

if [ ! $# -eq 2 ] ; then
  echo "usage: `basename $0` <Legacy Map> <GML Map>"
  exit 1
fi

CLASSPATH=../../jars/*:../../lib/* java maps.convert.legacy2gml.LegacyToGML $1 $2
