#!/bin/bash

. $(dirname $0)/config.sh

for i in $CLUSTERS; do
    SERVER=$(getServerHost $i)
    RUNNING_TEAM=""
    eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/boot/$LOCKFILE_NAME 2>/dev/null)
    if [ ! -z $RUNNING_TEAM ]; then
        if [ "$PRECOMPUTE" == "yes" ]; then
            echo " $i: $RUNNING_TEAM precomputing on $RUNNING_MAP"
	else
            echo " $i: $RUNNING_TEAM running on $RUNNING_MAP"
	fi
    else
        echo " $i: --- "
    fi
done