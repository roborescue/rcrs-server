#!/bin/bash
. $(dirname $0)/config.sh

for HOST in $HOSTS; do
    echo "$HOST: "
    scp -r $1 $REMOTE_USER@$HOST:$2
done
