#!/bin/bash

. $(dirname $0)/config.sh

CLUSTER=$1
MAP=$2
TEAM=$3
NAME=${TEAM_NAMES[$TEAM]}

SERVER=$(getServerHost $CLUSTER)

eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/scripts/$LOCKFILE_NAME 2>/dev/null)
if [ ! -z $RUNNING_TEAM ]; then
    echo "There is already a server running on cluster $CLUSTER"
    echo "${TEAM_NAMES[$RUNNING_TEAM]} ($RUNNING_TEAM) on $RUNNING_MAP"
    exit 1
fi;

echo "Starting run for team $NAME ($TEAM) on map $MAP on cluster $CLUSTER."

if [ -f "$CODEDIR/$TEAM/precompute.sh" ]; then
    echo "Starting kernel for precomputation..."
    
    ssh -X $REMOTE_USER@$SERVER $SCRIPTDIR/remoteStartKernelPrecompute.sh $MAP $TEAM&
    
    sleep 6
    if [ $MODE == "agent-all" ];then
        CLIENT=$(getClientHost $CLUSTER 1)
        ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartPrecompute.sh $TEAM $SERVER 4 $MAP&
    else
        for i in 1 2 3; do
            CLIENT=$(getClientHost $CLUSTER $i)
            ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartPrecompute.sh $TEAM $SERVER $i $MAP&
        done;
    fi
    sleep $PRECOMPUTE_TIMEOUT
    
    echo "stopping precomputation run"
    cancelRun.sh $CLUSTER
fi


echo "Starting kernel..."

ssh -X $REMOTE_USER@$SERVER $SCRIPTDIR/remoteStartKernel.sh $MAP $TEAM&

sleep 8

STATDIR=$LOCAL_HOMEDIR/$EVALDIR/$MAP/$TEAM
mkdir -p $STATDIR
#cd $LOCAL_HOMEDIR/$KERNELDIR/scripts
#./extract-view.sh $NAME $SERVER $STATDIR&
#cd $HOME

sleep 8
if [ $MODE == "agent-all" ];then
    CLIENT=$(getClientHost $CLUSTER 1)
    ssh $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartAgents.sh $TEAM $SERVER 4 $MAP&
else
    for i in 1 2 3; do
        CLIENT=$(getClientHost $CLUSTER $i)
        ssh  $REMOTE_USER@$CLIENT $SCRIPTDIR/remoteStartAgents.sh $TEAM $SERVER $i $MAP&
    done;
fi
cd $LOCAL_HOMEDIR/$KERNELDIR/scripts
export DISPLAY=172.19.0.2:0.0
./extract-view.sh $NAME $SERVER $STATDIR&
evpid=$!
cd $HOME
sleep 2

echo "Waiting fo run to finish..."

eval $(ssh -X $REMOTE_USER@$SERVER cat $KERNELDIR/scripts/$LOCKFILE_NAME 2>/dev/null)
while [ ! -z $RUNNING_TEAM ]; do
    sleep 5
    unset RUNNING_TEAM
    eval $(ssh $REMOTE_USER@$SERVER cat $KERNELDIR/scripts/$LOCKFILE_NAME 2>/dev/null)
    echo "Waiting fo run to finish... ${RUNNING_TEAM}"
done

echo "Evaluating run... $CLUSTER"

cancelRun.sh $CLUSTER

evalRun.sh $CLUSTER
kill $evpid
