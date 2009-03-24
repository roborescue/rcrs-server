#! /bin/sh
# $1: host where the kernel is running
java -cp ../programs/yab/sample/classes:../programs/yab/yab/classes sample.Main - - - - - - $*
