#!/bin/bash

if [ ! $# -eq 1 ] ; then
  echo "usage: `basename $0` <GML Map>"
  exit 1
fi

CLASSPATH=../../jars/*:../../lib/* java maps.gml.editor.GMLEditor $1
