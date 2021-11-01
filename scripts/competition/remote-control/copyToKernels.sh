#!/bin/bash
. $(dirname $0)/config.sh

for HOST in $SERVER_HOSTS; do
    scp -r $1 $REMOTE_USER@$HOST:$2
done
