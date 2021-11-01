#!/bin/bash
. $(dirname $0)/config.sh

for HOST in $CLIENT_HOSTS; do
    ssh $REMOTE_USER@$HOST $1
done
