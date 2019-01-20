#! /bin/bash

. functions.sh

processArgs $*

makeClasspath $BASEDIR/lib $BASEDIR/jars

java -Xmx1536m -cp $CP -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleFireBrigade*n sample.SampleAmbulanceTeam*n sample.SamplePoliceForce*n sample.SampleCentre*n -c $DIR/config/sample-agents.cfg 2>&1 | tee $LOGDIR/sample-out.log
