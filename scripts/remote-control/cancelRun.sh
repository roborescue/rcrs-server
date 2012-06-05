#!/bin/bash

. $(dirname $0)/config.sh

CLUSTER=$1
# MAP=$2
# TEAM=$3
# NAME=$TEAM_NAMES[$TEAM]

SERVER=$(getServerHost $CLUSTER)

eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/boot/$LOCKFILE_NAME 2>/dev/null)
if [ -z $PID ]; then
    echo "nothing seems to be running on cluster $CLUSTER"
else
    echo "killing kernel..."
    ssh $REMOTE_USER@$SERVER kill $PID
    ssh $REMOTE_USER@$SERVER killall java
    #ssh $REMOTE_USER@$SERVER rm $KERNELDIR/boot/$LOCKFILE_NAME
fi;

echo "killing clients"

for i in 1 2 3; do
    CLIENT=$(getClientHost $CLUSTER $i)
    ssh $REMOTE_USER@$CLIENT killall java
done;