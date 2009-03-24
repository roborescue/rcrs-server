#! /bin/sh
DIR=`dirname $0`

cd $DIR/../programs/firesimulator
java -cp .. firesimulator.Main -cstp $DIR/config.txt -stp default.stp -p 7000
