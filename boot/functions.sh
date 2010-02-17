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
    MAP="$BASEDIR/maps/gml/test"
    TEAM=""
    TIMESTAMP_LOGS=""

    while [[ ! -z "$1" ]]; do
        case "$1" in
            -m | --map)
                MAP="$2"
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
    KERNEL_OPTIONS="-c $DIR/config --gis.map.dir=$MAP --kernel.logname=$LOGDIR/rescue.log --kernel.simulators.auto= --kernel.viewers.auto= --kernel.agents.auto= --kernel.gis.auto=gis2.GMLWorldModelCreator --kernel.perception.auto=rescuecore2.standard.kernel.StandardPerception --kernel.communication.auto=rescuecore2.standard.kernel.ChannelCommunicationModel $*"
    makeClasspath $BASEDIR/jars $BASEDIR/lib
    xterm -T kernel -e "java -cp $CP kernel.StartKernel $KERNEL_OPTIONS -Dlog4j.debug=true 2>&1 | tee $LOGDIR/kernel-out.log" &
    PIDS="$PIDS $!"
    # Wait for the kernel to start
    waitFor $LOGDIR/kernel.log "Listening for connections"
}

# Start the viewer and simulators
function startSims {
    makeClasspath $BASEDIR/lib
    # Viewer
    xterm -T viewer -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleViewer $*" &
    PIDS="$PIDS $!"
    # Simulators
    xterm -T misc -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/misc.jar rescuecore2.LaunchComponents misc.MiscSimulator $*" &
    PIDS="$PIDS $!"
    #xterm -T traffic -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic-old.jar rescuecore2.LaunchComponents traffic.TrafficSimulatorWrapper 2>&1 | tee $LOGDIR/traffic.log" &
    xterm -T traffic -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic3.jar rescuecore2.LaunchComponents traffic3.simulator.TrafficSimulator $* 2>&1 | tee $LOGDIR/traffic-out.log" &
    PIDS="$PIDS $!"
    xterm -T fire -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/resq-fire.jar:$BASEDIR/oldsims/firesimulator/lib/commons-logging-1.1.1.jar rescuecore2.LaunchComponents firesimulator.FireSimulatorWrapper $* 2>&1 | tee $LOGDIR/fire.log" &
    PIDS="$PIDS $!"
    xterm -T ignition -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/ignition.jar rescuecore2.LaunchComponents ignition.IgnitionSimulator $*" &
    PIDS="$PIDS $!"
    xterm -T collapse -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/collapse.jar rescuecore2.LaunchComponents collapse.CollapseSimulator $*" &
    PIDS="$PIDS $!"
    xterm -T clear -e "java -Xmx256m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/clear.jar rescuecore2.LaunchComponents clear.ClearSimulator $*" &
    PIDS="$PIDS $!"
    xterm -T civilian -e "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar rescuecore2.LaunchComponents sample.SampleCivilian*n $*" &
    PIDS="$PIDS $!"
}
