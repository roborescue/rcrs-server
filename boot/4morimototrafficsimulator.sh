#! /bin/sh
DIR=`dirname $0`
java -cp $DIR/../programs/ traffic.Main localhost 7000 $*
