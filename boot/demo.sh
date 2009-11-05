#! /bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startKernel
startSims

xterm -T agents -e "java -Xmx256m -cp $BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleFireBrigade*n sample.SampleAmbulanceTeam*n sample.SamplePoliceForce*n sample.SampleCentre*n 2>&1 | tee $LOGDIR/agents.log" &
AGENTS=$!

waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $KERNEL $VIEWER $MISC $TRAFFIC $FIRE $BLOCKADES $COLLAPSE $CIVILIAN $AGENTS
