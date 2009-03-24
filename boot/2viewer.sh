#! /bin/sh
DIR=`dirname $0`
java -Xmx128m -cp $DIR/../programs/ viewer.Main -h localhost -p 7000 $*
