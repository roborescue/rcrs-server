#!/bin/bash

. $(dirname $0)/config.sh

for i in 1 2 3; do
    SERVER=$(getServerHost $i)
    RUNNING_TEAM=""
    eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/boot/$LOCKFILE_NAME 2>/dev/null)
    if [ ! -z $RUNNING_TEAM ]; then
        echo " $i: $RUNNING_TEAM on $RUNNING_MAP"
    else
        echo " $i: --- "
    fi
done