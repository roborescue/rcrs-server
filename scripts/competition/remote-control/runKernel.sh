#!/bin/bash

. $(dirname $0)/config.sh

CLUSTER=$1
MAP=$2
TEAM=$3
NAME=${TEAM_NAMES[$TEAM]}

SERVER=$(getServerHost $CLUSTER)

eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/boot/$LOCKFILE_NAME 2>/dev/null)
if [ ! -z $RUNNING_TEAM ]; then
    echo "There is already a server running on cluster $CLUSTER"
    echo "${TEAM_NAMES[$RUNNING_TEAM]} ($RUNNING_TEAM) on $RUNNING_MAP"
    exit 1
fi;

echo "Starting run for team $NAME ($TEAM) on map $MAP on cluster $CLUSTER."
echo "Starting kernel..."

ssh $REMOTE_USER@$SERVER $SCRIPTDIR/remoteStartKernel.sh $MAP $NAME&
