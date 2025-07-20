#! /bin/bash

. $(dirname $0)/config.sh

TEAM=$1
HOST=$2
TYPE=$3
MAP=$4

declare -A CODES
CODES[4]=all
CODES[1]=fire
CODES[2]=police
CODES[3]=ambulance

T=$(date +"%d-%m-%Y--%k-%M-%S")
LOGFILE=$HOME/$LOGDIR/$DAY/$TEAM/$MAP-$T-${CODES[$TYPE]}.log

mkdir -p $HOME/$LOGDIR/$DAY/$TEAM

cd $HOME/$CODEDIR/$TEAM

echo Starting agents on machine $(hostname)

if (( $TYPE == 4)); then
    echo "Running: /bin/sh \"start.sh\" -1 -1 -1 -1 -1 -1 $HOST 2>&1 | tee $LOGFILE"
   /bin/sh "start.sh"  -1 -1 -1 -1 -1 -1 $HOST 2>&1 | tee $LOGFILE

fi
# firebrigade
if (( $TYPE == 1)); then
    echo "Running: /bin/sh \"start.sh\" -1 -1 0 0 0 0 $HOST 2>&1 | tee $LOGFILE"
    /bin/sh "start.sh"  -1 -1 0 0 0 0 $HOST 2>&1 | tee $LOGFILE
#   /bin/sh "start.sh"  -1 -1 -1 -1 -1 -1 $HOST 2>&1 | tee $LOGFILE

fi

# policeforce
if (( $TYPE == 2)); then
    echo "Running: /bin/sh \"start.sh\" 0 0 -1 -1 0 0 $HOST 2>&1 | tee $LOGFILE"
    /bin/sh "start.sh" 0 0 -1 -1 0 0 $HOST 2>&1 | tee $LOGFILE
fi

# ambulance
if (( $TYPE == 3)); then
    echo "Running: /bin/sh \"start.sh\" 0 0 0 0 -1 -1 $HOST 2>&1 | tee $LOGFILE"
    /bin/sh "start.sh" 0 0 0 0 -1 -1 $HOST 2>&1 | tee $LOGFILE
fi
