#! /bin/sh
DIR=`dirname $0`/../jars/rescuecore2.jar
java -Xmx256m -cp $DIR/ rescuecore2.sample.LaunchSampleAgents
