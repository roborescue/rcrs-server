#! /bin/sh

# The directory to store logs.
LOGDIR=""

DIR=`dirname $0`
MAP="$1"
TEAM="$2"
if [ -z $MAP ] ; then
  MAP=Kobe
fi
if [ ! -d $MAP ] ; then
  MAP="$DIR/../maps/$MAP"
fi

LOG="rescue.log"
if [ ! -z $TEAM ] ; then
  LOG="`date +%m%d-%H%M%S`-$TEAM-`basename $MAP`.log"
fi
if [ ! -z $LOGDIR ] ; then
  LOG="$LOGDIR/$LOG"
fi

LD_COMMAND="export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DIR/../src/librescue"

OPTIONS="--mapdir $MAP --config $DIR/config.txt"

xterm -T gis -e "$LD_COMMAND; $DIR/0gis.sh $OPTIONS 2>&1 | tee gis.log" &
sleep 2
xterm -T kernel -e "$LD_COMMAND; $DIR/1kernel.sh $OPTIONS --logname $LOG 2>&1 | tee kernel.log" &
sleep 2
if [ -z $TEAM ]; then
	xterm -T viewer -e "$LD_COMMAND; $DIR/2viewer.sh 2>&1 | tee viewer.log" &
else
	xterm -T viewer -e "$LD_COMMAND; $DIR/2viewer.sh -t $TEAM 2>&1 | tee viewer.log" &
fi
xterm -T misc -e "$LD_COMMAND; $DIR/3miscsimulator.sh $OPTIONS 2>&1 | tee misc.log" &
xterm -T traffic -e "$LD_COMMAND; $DIR/4morimototrafficsimulator.sh 2>&1 | tee traffic.log" &
xterm -T fire -e "$LD_COMMAND; $DIR/5firesimulator.sh $OPTIONS 2>&1 | tee fire.log" &
xterm -T blockades -e "$LD_COMMAND; $DIR/6blockadessimulator.sh $OPTIONS 2>&1 | tee blockades.log" &
xterm -T collapse -e "$LD_COMMAND; $DIR/7collapsesimulator.sh $OPTIONS 2>&1 | tee collapse.log" &
sleep 2

xterm -T civilian -e "$LD_COMMAND; $DIR/8civilian.sh $OPTIONS 2>&1 | tee civilian.log" &

sleep 2

echo "Start your agents"