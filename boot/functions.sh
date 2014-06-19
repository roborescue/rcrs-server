DIR=`pwd`
BASEDIR="`cd .. && pwd`"
PIDS=

# Wait for a regular expression to appear in a file.
# $1 is the log to check
# $2 is the regex to wait for
# $3 is the optional output frequency. Messages will be output every n sleeps. Default 1.
# $4 is the optional sleep time. Defaults to 1 second.
function waitFor {
    SLEEP_TIME=1
    FREQUENCY=1
    if [ ! -z "$3" ]; then
        FREQUENCY=$3
    fi
    if [ ! -z "$4" ]; then
        SLEEP_TIME=$4
    fi
    F=$FREQUENCY
    echo "Waiting for '$1' to exist..."
    while [[ ! -e $1 ]]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$1' to exist..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
    echo "Waiting for '$2'..."
    while [ -z "`grep \"$2\" \"$1\"`" ]; do
        if (( --F == 0 )); then
            echo "Still waiting for '$2'..."
            F=$FREQUENCY
        fi
        sleep $SLEEP_TIME
    done
}

# Make a classpath argument by looking in a directory of jar files.
# Positional parameters are the directories to look in
function makeClasspath {
    RESULT="../supplement"
    while [[ ! -z "$1" ]]; do
        for NEXT in $1/*.jar; do
            RESULT="$RESULT:$NEXT"
        done
        shift
    done
    CP=${RESULT#:}
}

# Print the usage statement
function printUsage {
    echo "Usage: $0 [options]"
    echo "Options"
    echo "======="
    echo "-m    --map       <mapdir>      Set the map directory. Default is \"$BASEDIR/maps/gml/test\""
    echo "-l    --log       <logdir>      Set the log directory. Default is \"logs\""
    echo "-s    --timestamp               Append a timestamp, the team name and map name to the log directory name"
    echo "-t    --team      <teamname>    Set the team name. Default is \"\""
}

# Process arguments
function processArgs {
    LOGDIR="logs"
    MAP="$BASEDIR/maps/gml/Kobe2013/map"
    CONFIGDIR="$BASEDIR/maps/gml/Kobe2013/config"
    TEAM=""
    TIMESTAMP_LOGS=""

    while [[ ! -z "$1" ]]; do
        case "$1" in
            -m | --map)
                MAP="$2"
                shift 2
                ;;
            -c | --config)
                CONFIGDIR="$2"
                shift 2
                ;;
            -l | --log)
                LOGDIR="$2"
                shift 2
                ;;
            -t | --team)
                TEAM="$2"
                shift 2
                ;;
            -s | --timestamp)
                TIMESTAMP_LOGS="yes";
                shift
                ;;
            -h | --help)
                printUsage
                exit 1;
                ;;
            
            *)
                echo "Unrecognised option: $1"
                printUsage
                exit 1
                ;;
        esac
    done

    if [ -z $MAP ] ; then
        printUsage
        exit 1
    fi
    if [ ! -d $MAP ] ; then
        echo "$MAP is not a directory"
        printUsage
        exit 1
    fi

    if [ ! -z "$TIMESTAMP_LOGS" ] ; then
        TIME="`date +%m%d-%H%M%S`"
        MAPNAME="`basename $MAP`"
        if [ "$MAPNAME" == "map" ]; then
            MAPNAME="$(basename $(dirname $MAP))"
        fi
        if [ -z "$TEAM" ]; then
            LOGDIR="$LOGDIR/$TIME-$MAPNAME"
        else
            LOGDIR="$LOGDIR/$TIME-$TEAM-$MAPNAME"
        fi
    fi
    LOGDIR=`readlink -f $LOGDIR`
    mkdir -p $LOGDIR
}

# Start the kernel
function startKernel {
    KERNEL_OPTIONS="-c $CONFIGDIR/kernel.cfg --gis.map.dir=$MAP --kernel.logname=$LOGDIR/rescue.log $*"
    makeClasspath $BASEDIR/jars $BASEDIR/lib

    xterm -T kernel -e "java -Xmx2048m -cp $CP -Dlog4j.log.dir=$LOGDIR kernel.StartKernel $KERNEL_OPTIONS 2>&1 | tee $LOGDIR/kernel-out.log" &
    PIDS="$PIDS $!"
    # Wait for the kernel to start
    waitFor $LOGDIR/kernel.log "Listening for connections"
}

# Start the viewer and simulators
function startSims {
    makeClasspath $BASEDIR/lib
    # Viewer
    TEAM_NAME_ARG=""
    if [ ! -z "$TEAM" ]; then
        TEAM_NAME_ARG="\"--viewer.team-name=$TEAM\"";
    fi

    # xterm -T human -e "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar:$BASEDIR/jars/human.jar human.ControlledAgentGUI -c $CONFIGDIR/human.cfg $* 2>&1 | tee $LOGDIR/human-out.log" &
    # PIDS="$PIDS $!"
    # Simulators

    xterm -T misc -e "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/misc.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents misc.MiscSimulator -c $CONFIGDIR/misc.cfg $* 2>&1 | tee $LOGDIR/misc-out.log" &
    PIDS="$PIDS $!"
    xterm -T traffic -e "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic3.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents traffic3.simulator.TrafficSimulator -c $CONFIGDIR/traffic3.cfg $* 2>&1 | tee $LOGDIR/traffic-out.log" &
    PIDS="$PIDS $!"
    xterm -T fire -e "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/resq-fire.jar:$BASEDIR/oldsims/firesimulator/lib/commons-logging-1.1.1.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents firesimulator.FireSimulatorWrapper -c $CONFIGDIR/resq-fire.cfg $* 2>&1 | tee $LOGDIR/fire-out.log" &
    PIDS="$PIDS $!"
    xterm -T ignition -e "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/ignition.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents ignition.IgnitionSimulator -c $CONFIGDIR/ignition.cfg $* 2>&1 | tee $LOGDIR/ignition-out.log" &
    PIDS="$PIDS $!"
    xterm -T collapse -e "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/collapse.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents collapse.CollapseSimulator -c $CONFIGDIR/collapse.cfg $* 2>&1 | tee $LOGDIR/collapse-out.log" &
    PIDS="$PIDS $!"
    xterm -T clear -e "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/clear.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents clear.ClearSimulator -c $CONFIGDIR/clear.cfg $* 2>&1 | tee $LOGDIR/clear-out.log" &
    PIDS="$PIDS $!"

echo "waiting for misc to connect..."
    waitFor $LOGDIR/misc-out.log "success"
echo "waiting for traffic to connect..."
    waitFor $LOGDIR/traffic-out.log "success"

echo "waiting for fire to connect..."
    waitFor $LOGDIR/fire-out.log "success"
echo "waiting for ignition to connect..."
    waitFor $LOGDIR/ignition-out.log "success"
echo "waiting for collapse to connect..."
    waitFor $LOGDIR/collapse-out.log "success"
echo "waiting for clear to connect..."    
    waitFor $LOGDIR/clear-out.log "success"

    xterm -T civilian -e "java -Xmx1324m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar:$BASEDIR/jars/kernel.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleCivilian*n -c $CONFIGDIR/civilian.cfg $* 2>&1 | tee $LOGDIR/civilian-out.log" &
    PIDS="$PIDS $!"
sleep 2
    xterm -T viewer -e "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleViewer -c $CONFIGDIR/viewer.cfg $TEAM_NAME_ARG $* 2>&1 | tee $LOGDIR/viewer-out.log" &
    PIDS="$PIDS $!"



    # Wait for all simulators to start
echo "waiting for viewer to connect..."
    waitFor $LOGDIR/viewer-out.log "success"

}
