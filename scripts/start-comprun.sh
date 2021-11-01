#!/bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

# startGIS
startKernel --nomenu --autorun
startSims
startViewer

echo "Start your agents"
waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $PIDS