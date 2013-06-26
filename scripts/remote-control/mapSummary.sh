#!/bin/bash
. $(dirname $0)/config.sh

MAP=$1
MAP_EVALDIR=$HOME/$EVALDIR/$MAP
EVAL_SCRIPTS=$HOME/$KERNELDIR/scripts/evaluation
PATH=$PATH:$EVAL_SCRIPTS

cd $MAP_EVALDIR

if [ -f scores.tmp ]; then
    rm scores.tmp
fi

if [ -f final-scores.txt ]; then
    rm final-scores.txt
fi


NUM_PROCESSED=0
for TEAM in $TEAM_SHORTHANDS; do
    NAME=${TEAM_NAMES[$TEAM]}
    if [ -s $TEAM/init-score.txt ]; then
        cp $TEAM/init-score.txt init-score.txt
        echo -n "\"$NAME\" ">> scores.tmp
        cat $TEAM/scores.txt >> scores.tmp
        echo >> scores.tmp
        # echo -n "$TEAM " >> final-scores.txt
        # cat $TEAM/final-score.txt >> final-scores.txt
        # echo >> final-scores.txt

        for screen in $TEAM/*.png; do
            tn=${screen%.png}-tn.jpg
            convert -format jpg -thumbnail 100x75 -strip -quality 95 PNG8:"$screen" "$tn"
        done
        if [ ! -f snapshot-init.png ]; then
            cp $TEAM/snapshot-init.png snapshot-init.png
            convert -format png -thumbnail 400x300 -strip -quality 95 PNG8:snapshot-init.png snapshot-init-small.png
        fi
        LOGFILES=$(ls $HOME/$LOGDIR/$DAY/kernel/*$NAME-$MAP.gz 2>/dev/null) 
        if [[ -f "$LOGFILES" && ! -f $MAP_EVALDIR/$LOGFILES ]]; then
            cp $LOGFILES $MAP_EVALDIR
        fi;
        NUM_PROCESSED=$((NUM_PROCESSED+1))
#        echo "recompressing ..."
#        bzip2 $LOGFILE
      fi
done


# create map tgz
if [[ -d $HOME/$MAPDIR/$MAP && ! -f $MAP_EVALDIR/$MAP.tgz  ]]; then
    cd $HOME/$MAPDIR
    tar czf $MAP_EVALDIR/$MAP.tgz $MAP
fi;

cd $MAP_EVALDIR

transpose.py scores.tmp > scores.dat
export RCR_MAP=$MAP
export RCR_COUNT=$NUM_PROCESSED
echo $RCR_COUNT teams processed
gnuplot $EVAL_SCRIPTS/plot-scores.gnu

$EVAL_SCRIPTS/make_html.py $MAP > index.html