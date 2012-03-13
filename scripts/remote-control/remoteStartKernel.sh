#! /bin/bash

. $(dirname $0)/config.sh

MAP=$1
TEAM=$2

export DISPLAY=:0

cd $HOME

if [ -d $MAPDIR/$MAP/config ]; then
    CONFIG=$HOME/$MAPDIR/$MAP/config
else
    CONFIG=config
fi

if [ -d $MAPDIR/$MAP/map ]; then
    THISMAPDIR=$HOME/$MAPDIR/$MAP/map
else
    THISMAPDIR=$HOME/$MAPDIR/$MAP
fi

TIME="`date +%m%d-%H%M%S`"
MAPNAME="`basename $MAP`"

KERNEL_LOGDIR=$HOME/simlogs/$TIME-$TEAM-$MAPNAME

cd $KERNELDIR/boot

echo "RUNNING_TEAM=$TEAM" >> $LOCKFILE_NAME
echo "RUNNING_MAP=$MAP" >> $LOCKFILE_NAME

./start.sh -m $THISMAPDIR -c $CONFIG -t $TEAM -l $KERNEL_LOGDIR &
echo "PID=$!" >> $LOCKFILE_NAME

wait

echo "Zipping logfile..."
mkdir -p $HOME/$LOGDIR/$DAY/kernel/
cp $KERNEL_LOGDIR/rescue.log $HOME/$LOGDIR/$DAY/kernel/$TIME-$TEAM-$MAPNAME
gzip $HOME/$LOGDIR/$DAY/kernel/$TIME-$TEAM-$MAPNAME

rm $LOCKFILE_NAME
echo "All done"