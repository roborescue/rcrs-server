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
    echo "Usage: $0 [scenario] [options]"
    echo
    echo "[scenario]  Scenario directory including the map and config directories. Default: \"../maps/gml/test\""
    echo
    echo "[options]"
    echo "-m    --map       <mapdir>    Set the map directory. Default: \"../maps/gml/test/map\""
    echo "-c    --config    <configdir> Set the config directory. Default: \"../maps/gml/test/config\""
    echo "-t    --team      <teamname>  Set the team name. Default: \"\""
    echo "-l    --log       <logdir>    Set the log directory. Default: \"logs\""
    echo "-s    --timestamp             Create a log sub-directory including timestamp, team name and map name"
    echo "[+|-]x                        Enable/Disable XTerm use. Default: \"Disable\""
}

# Process command-line arguments
function processArgs {
    LOGDIR="logs"
    MAP="$BASEDIR/maps/gml/test/map"
    CONFIGDIR="$BASEDIR/maps/gml/test/config"
    TEAM=""
    TIMESTAMP_LOGS=""
    XTERM="no"
    
    if [ $# -gt 0 ] && [[ $1 != -* ]]; then
        MAP="$1/map"
        CONFIGDIR="$1/config"
        shift 1
    fi
    
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
                TIMESTAMP_LOGS="yes"
                shift
                ;;
            -x)
                XTERM="no"
                shift
                ;;
            +x)
                XTERM="yes"
                shift
                ;;
            -h | --help)
                printUsage
                exit 1
                ;;
            *)
                echo "Unrecognized option: $1"
                printUsage
                exit 1
                ;;
        esac
    done
    
    # Check if the Map directory exists
    if [ -z $MAP ] || [ ! -d $MAP ]; then
    echo "Directory does not exist or does not have the \"map\" and \"config\" sub-directories"
        exit 1
    fi
    
    # Append timestamp, team and map name to the log directory
    if [ ! -z "$TIMESTAMP_LOGS" ]; then
        TIME="`date +%Y%m%d-%H%M%S`"
        
        # Extract map name
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

function execute {
    title=$1
    command=$2
    if [[ $XTERM == "yes" ]];then
        xterm -T $title -e "$command  2>&1 |tee $LOGDIR/$title-out.log" &
    else
        sh -c "$command  2>&1 |tee $LOGDIR/$title-out.log" &
    fi
    PIDS="$PIDS $!"
}

# Start the kernel
function startKernel {
    KERNEL_OPTIONS="-c $CONFIGDIR/kernel.cfg --gis.map.dir=$MAP --kernel.logname=$LOGDIR/rescue.log $*"
    makeClasspath $BASEDIR/jars $BASEDIR/lib

    execute kernel "java -Xmx2048m -cp $CP -Dlog4j.log.dir=$LOGDIR kernel.StartKernel $KERNEL_OPTIONS"
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
    
    # Execute the simulators
    execute misc "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/misc.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents misc.MiscSimulator -c $CONFIGDIR/misc.cfg $*"
    execute traffic "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/traffic3.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents traffic3.simulator.TrafficSimulator -c $CONFIGDIR/traffic3.cfg $*"
    execute fire "java -Xmx1024m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/resq-fire.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents firesimulator.FireSimulatorWrapper -c $CONFIGDIR/resq-fire.cfg $*"
    execute ignition "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/ignition.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents ignition.IgnitionSimulator -c $CONFIGDIR/ignition.cfg $*"
    execute collapse "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/collapse.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents collapse.CollapseSimulator -c $CONFIGDIR/collapse.cfg $*"
    execute clear "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/clear.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents clear.ClearSimulator -c $CONFIGDIR/clear.cfg $*"
    
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
    
    execute civilian "java -Xmx1512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar:$BASEDIR/jars/kernel.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleCivilian*n -c $CONFIGDIR/civilian.cfg $*"
    sleep 2
    execute viewer "java -Xmx512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar -Dlog4j.log.dir=$LOGDIR rescuecore2.LaunchComponents sample.SampleViewer -c $CONFIGDIR/viewer.cfg $TEAM_NAME_ARG $*"
    
    # Wait for all simulators to start
    echo "waiting for viewer to connect..."
    waitFor $LOGDIR/viewer-out.log "success"
}
