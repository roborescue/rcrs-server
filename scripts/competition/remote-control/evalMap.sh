#!/bin/bash
. $(dirname $0)/config.sh
MAP=$1
echo $MAP

for TEAM in $TEAM_SHORTHANDS; do
    NAME=${TEAM_NAMES[$TEAM]}
    
    LOGFILES=$(ls $HOME/$LOGDIR/*/kernel/*$NAME-$MAP.[x7]z 2>/dev/null)
    if [ "$LOGFILES" == "" ];then
    echo "no log is availble for $MAP $TEAM"
    else
    echo $LOGFILES
    evalLog.sh $LOGFILES $MAP $TEAM
    fi 
done