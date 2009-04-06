#! /bin/sh
DIR=`dirname $0`

cd $DIR/../src/firesimulator
java -Xmx256m -cp .. firesimulator.Main -cstp $DIR/config.txt -stp default.stp -p 7000
