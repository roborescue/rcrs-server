#! /bin/sh
DIR=`dirname $0`
java -Xmx256m -cp $DIR/../src/ viewer.Main -h localhost -p 7000 $*
