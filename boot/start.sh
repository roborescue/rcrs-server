#! /bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startKernel
startSims

echo "Start your agents"
waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $KERNEL $VIEWER $MISC $TRAFFIC $FIRE $BLOCKADES $COLLAPSE
