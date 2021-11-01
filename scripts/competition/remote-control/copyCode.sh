#!/bin/bash
. $(dirname $0)/config.sh

if [ "$1" == "" ]; then
    TEAMS=$TEAM_SHORTHANDS
else
    TEAMS=$1
fi

for t in $TEAMS; do
    TEAM_USER=$(echo $t | tr '[A-Z]' '[a-z]')
    rsync -rcLv $RSYNC_OPTS /home/$TEAM_USER/code/ /home/$LOCAL_USER/code/$TEAM/
done
