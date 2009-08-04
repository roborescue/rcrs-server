#! /bin/sh
DIR=`dirname $0`

cd $DIR/../oldsims/firesimulator
java -Xmx256m -cp .. firesimulator.Main -cstp $DIR/config.txt -stp default.stp -p 7000
