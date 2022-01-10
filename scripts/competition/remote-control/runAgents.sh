#!/bin/bash

. $(dirname $0)/config.sh

CLUSTER=$1
MAP=$2
TEAM=$3
NAME=${TEAM_NAMES[$TEAM]}

SERVER=$(getServerHost $CLUSTER)

for i in 1 2 3; do
    CLIENT=$(getClientHost $CLUSTER $i)
    ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartAgents.sh $TEAM $SERVER $i $MAP&
done;
