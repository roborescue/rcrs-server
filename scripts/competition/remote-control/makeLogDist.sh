#!/bin/bash

. $(dirname $0)/config.sh

MAPS="VC1 Paris1 Kobe1 Berlin1 Istanbul1 Kobe2 Paris2 Istanbul2 Berlin2 VC2 Paris3 Istanbul3 Berlin3 Kobe3 Istanbul4 Berlin4 Paris4 VC4 Paris5 Berlin5 Kobe4 Istanbul5 VC5"

DIR=$(pwd)

for map in $MAPS; do
    FILES=$(find $LOGDIR -name *$map*|grep kernel)
    mkdir -p $DISTDIR/$map
    for f in $FILES; do
        cp "$f" $DISTDIR/$map
    done
done
