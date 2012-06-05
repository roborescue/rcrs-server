#!/bin/bash
. $(dirname $0)/config.sh

PATH=$PATH:$HOME/$KERNELDIR/scripts/evaluation

cd $HOME/$EVALDIR

for DIR in *; do
    if [[ -d $DIR && ! -s $DIR/index.html ]]; then
        mapSummary.sh $DIR
    fi;
done;

make_overview.py > index.html
