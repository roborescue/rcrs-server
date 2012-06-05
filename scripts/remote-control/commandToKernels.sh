#!/bin/bash
. $(dirname $0)/config.sh

for HOST in $SERVER_HOSTS; do
    ssh $REMOTE_USER@$HOST $*
done

