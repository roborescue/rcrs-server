#! /bin/sh
# $1: host where the kernel is running
java -cp sample/classes:yab/classes sample.Main - - - - - - $*
