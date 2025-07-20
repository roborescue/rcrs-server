#!/bin/bash
. $(dirname $0)/config.sh

if [[ $1 == "" ]];then
   echo "usage: sync...sh srcfolder [dstfolder]"
   exit 1
fi


target=${2:-$1}

for HOST in $HOSTS; do
    echo "$HOST: "
    rsync -rcLv $RSYNC_OPTS $1/ $REMOTE_USER@$HOST:$target/ &
done

wait
