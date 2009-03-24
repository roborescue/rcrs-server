#! /bin/sh
DIR=`dirname $0`
java -Xmx128m -cp $DIR/../program/kuwataviewer/LV153.jar viewer.Map -h localhost $*
