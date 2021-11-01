#!/bin/bash
. $(dirname $0)/config.sh

for HOST in $HOSTS; do
   ssh $REMOTE_USER@$HOST  "echo $HOST"

done
