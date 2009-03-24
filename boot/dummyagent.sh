#! /bin/sh
DIR=`dirname $0`/../programs
java -Xmx256m -cp $DIR/ rescuecore.Launch localhost 7000 "0 rescuecore.DummyAgent"
