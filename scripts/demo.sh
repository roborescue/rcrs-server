#!/bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startKernel --autorun --nomenu
startSims --nogui
startViewer --viewer.team-name=Sample --viewer.maximise=true

makeClasspath $BASEDIR/lib
xterm -T agents -e "./sampleagent.sh" &
PIDS="$PIDS $!"

waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $PIDS