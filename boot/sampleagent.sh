#! /bin/sh
DIR=`dirname $0`/../jars/
java -Xmx256m -cp $DIR/rescuecore2.jar:$DIR/standard.jar:$DIR/sample.jar sample.LaunchSampleAgents
