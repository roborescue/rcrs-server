#! /bin/sh

# The directory to store logs.
LOGDIR="logs"


if [ $# -ne 2 ]; then
	echo "Usage: ./all2.sh Mapname Teamname"
	exit 0
fi

DIR=`dirname $0`
MAP="$1"
if [ -z $MAP ] ; then
	echo "You must specify a map"
	exit 0
fi
if [ ! -d $MAP ] ; then
  MAP="$DIR/../maps/$MAP"
fi

if [ ! -d $LOGDIR ]; then
	mkdir $LOGDIR
fi

if [ -e action.log ]; then
	mv action.log "$LOGDIR/`cat prev_info`" #>> /dev/null
fi

echo "action-`date +%m%d-%H%M%S`-$2-`basename $MAP`.log" > prev_info

LOG="rescue.log"
if [ ! -z $2 ] ; then
  LOG="`date +%m%d-%H%M%S`-$2-`basename $MAP`.log"
fi
if [ ! -z $LOGDIR ] ; then
  LOG="$LOGDIR/$LOG"
fi

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DIR/../src/librescue

OPTIONS="-logname $LOG -shindopolydata $MAP/shindopolydata.dat -galpolydata $MAP/galpolydata.dat"

PIDS=

$DIR/0gis.sh $OPTIONS -mapdir $MAP/ -gisini $MAP/gisini.txt >& gis.log &
sleep 2
#xterm -e $DIR/1kernel.sh $OPTIONS &
$DIR/1kernel.sh $OPTIONS >& kernel.log &
sleep 2
#xterm -e ./kuwataviewer.sh -l 300 &
$DIR/2viewer.sh -t $TEAM >& viewer.log &
$DIR/3miscsimulator.sh $OPTIONS >& misc.log &
$DIR/4morimototrafficsimulator.sh >& traffic.log &
$DIR/5firesimulator.sh $OPTIONS >& fire.log &
$DIR/6blockadessimulator.sh $OPTIONS >& blockade.log &
$DIR/7collapsesimulator.sh $OPTIONS >& collapse.log &
sleep 2

$DIR/8civilian.sh >& civilian.log &
sleep 2
echo "Start your agents"
echo "Press enter to end simulation"

read
killall gis kernel blockadessimulator collapsesimulator civilian miscsimulator

sleep 1
if [ -e action.log ]; then
mv action.log "$LOGDIR/`cat prev_info`"
fi
