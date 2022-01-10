#! /bin/bash

. $(dirname $0)/config.sh

MAPS="$*"

function getFreeCluster() {
COUNT="0 0 0"
while [ 1 ];do
for i in $CLUSTERS; do
    SERVER=$(getServerHost $i)
    RUNNING_TEAM=""
    PRECOMPUTE=""
    TURN="-"
    eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/boot/$LOCKFILE_NAME 2>/dev/null)
    if [ ! -z $RUNNING_TEAM ]; then
	COUNT[$i]=0
    else
	COUNT[$i]=$(( ${COUNT[$i]} + 1 ))
    fi
	echo "cluster" $i "FREE " ${COUNT[$i]} "Time"
   if [ ${COUNT[$i]} -eq 5 ];then
	FREE=$i
	return;
   fi


done
	sleep 10
done
}


FREE=0


ALL=()
count=0;
uploadMaps.sh

TEAM_SHORTHANDS2=$(shuf -e $TEAM_SHORTHANDS)
echo $TEAM_SHORTHANDS2
echo "MAPS $MAPS"
echo "=========================="
for MAP in $MAPS; do
#	echo $MAP

	for TEAM in $TEAM_SHORTHANDS2; do
#	echo $TEAM
		ALL+="$MAP-$TEAM "
		count=$(( $count + 1 ))
	done
done
for item in $ALL; do
	arr=$(echo $item | tr "-" "\n")
	MAP=${arr[0]}
	TEAM=${arr[1]}

	getFreeCluster
	run.sh $FREE $MAP $TEAM &
	#xterm -T "run.sh $FREE $MAP $TEAM" -e "run.sh $FREE $MAP $TEAM" &
done
