#! /bin/bash

. $(dirname $0)/config.sh

MAP=$1

cd $HOME/$EVALDIR

NUM_PROCESSED=0
for t in $TEAM_SHORTHANDS; do
    NAME=${TEAM_NAMES[$t]}
    logs=($MAP/*-$NAME-*.7z)
    log=${logs[@]}
    if [ -f $log ]; then
	rsync -ave ssh $log $SOURCEFORGE_USER@frs.sf.net:/home/frs/project/roborescue/logs/$YEAR/$MAP/
	if [ "$?" == 0 ]; then
            NUM_PROCESSED=$((NUM_PROCESSED+1))
	fi
    else
	echo "Logfile for $NAME not found or not unique: $log"
    fi
done

echo "Uploaded $NUM_PROCESSED files"
