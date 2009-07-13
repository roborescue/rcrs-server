#! /bin/sh

# The directory to store logs.
LOGDIR=""

DIR=`dirname $0`
MAP="Kobe"

LD_COMMAND="export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$DIR/../src/librescue"

OPTIONS="--mapdir ../maps/$MAP --config $DIR/config.txt"

xterm -T viewer -e "$DIR/2viewer.sh 2>&1 | tee viewer.log" &
VIEWER=$!
xterm -T misc -e "$LD_COMMAND; $DIR/3miscsimulator.sh $OPTIONS 2>&1 | tee misc.log" &
MISC=$!
xterm -T traffic -e "$DIR/4morimototrafficsimulator.sh 2>&1 | tee traffic.log" &
TRAFFIC=$!
xterm -T fire -e "$DIR/5firesimulator.sh $OPTIONS 2>&1 | tee fire.log" &
FIRE=$!
xterm -T blockades -e "$LD_COMMAND; $DIR/6blockadessimulator.sh $OPTIONS 2>&1 | tee blockades.log" &
BLOCKADES=$!
xterm -T collapse -e "$LD_COMMAND; $DIR/7collapsesimulator.sh $OPTIONS 2>&1 | tee collapse.log" &
COLLAPSE=$!

sleep 2

#xterm -T civilian -e "$LD_COMMAND; $DIR/8civilian.sh $OPTIONS 2>&1 | tee civilian.log" &
#CIVILIAN=$!
#xterm -T agents -e "$DIR/sampleagent.sh 2>&1 | tee agents.log" &
#AGENTS=$!

read INPUT

kill $VIEWER $MISC $TRAFFIC $FIRE $BLOCKADES $COLLAPSE $CIVILIAN $AGENTS