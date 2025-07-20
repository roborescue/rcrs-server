#!/bin/bash

. $(dirname $0)/config.sh

CLUSTER=$1
MAP=$2
TEAM=$3
NAME=${TEAM_NAMES[$TEAM]}

SERVER=$(getServerHost $CLUSTER)

if [ $MODE == "agent-all" ];then
 CLIENT=$(getClientHost $CLUSTER 1)
 ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartAgents.sh $TEAM $SERVER 4 $MAP&
else
 for i in 1 2 3 ; do
    CLIENT=$(getClientHost $CLUSTER $i)
    ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartAgents.sh $TEAM $SERVER $i $MAP&
 done;
fi
