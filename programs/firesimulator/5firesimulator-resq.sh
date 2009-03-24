#! /bin/sh
DIR=`dirname $0`

cd $DIR/../program/firesimulator/ && java Main -cstp $DIR/config.txt -stp default.stp
