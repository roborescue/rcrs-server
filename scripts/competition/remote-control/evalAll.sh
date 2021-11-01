#!/bin/bash
. $(dirname $0)/config.sh

PATH=$PATH:$HOME/scripts/evaluation

cd $HOME/$EVALDIR

for DIR in *; do
       echo $DIR "==="
    #if [[ -d $DIR && ! -s $DIR/index.html ]]; then
     if [[ $DIR != "viewer" ]]; then
       mapSummary.sh $DIR ||echo "error"
#	echo $DIR
    fi;
done;

make_overview.py > index.html
