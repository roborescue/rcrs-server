#!/bin/bash
. $(dirname $0)/config.sh

echo "unpacking..."
zcat $1 > rescue.log.tmp

DIR=$(pwd)
cd $KERNELDIR/boot
./getScore.sh $DIR/rescue.log.tmp
cd $DIR
rm rescue.log.tmp
