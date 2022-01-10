#!/bin/bash
. $(dirname $0)/config.sh

if [ "$1" == "" ]; then
    TEAMS=$TEAM_SHORTHANDS
else
    TEAMS=$1
fi

for t in $TEAMS; do
    cd $LOCAL_HOMEDIR/code/$TEAM/
    ./compile.sh
done
