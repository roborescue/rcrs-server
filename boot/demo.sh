#! /bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startKernel
startSims

java -Xmx256m -cp $BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar sample.LaunchSampleAgents >& $LOGDIR/agents.log &
AGENTS=$!

waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $KERNEL $VIEWER $MISC $TRAFFIC $FIRE $BLOCKADES $COLLAPSE $CIVILIAN $AGENTS
