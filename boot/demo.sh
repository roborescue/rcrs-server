#! /bin/bash

. functions.sh

processArgs $*

# Delete old logs
rm -f $LOGDIR/*.log

startKernel --autorun --nomenu
startSims --nogui

makeClasspath $BASEDIR/lib
xterm -T agents -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleFireBrigade*n sample.SampleAmbulanceTeam*n sample.SamplePoliceForce*n sample.SampleCentre*n 2>&1 | tee $LOGDIR/agents-out.log" &
PIDS="$PIDS $!"

waitFor $LOGDIR/kernel.log "Kernel has shut down" 30

kill $PIDS
