#!/bin/bash
. $(dirname $0)/config.sh

MAP=$1
EVALDIR=$DIR/$MAP-eval

if [ ! -d $EVALDIR ]; then
    mkdir $EVALDIR
    cd $EVALDIR

    echo "extracting $MAP.tar"
    tar xvf ../$MAP.tar
fi
cd $EVALDIR

if [ -f scores.tmp ]; then
    rm scores.tmp
fi

if [ -f final-scores.txt ]; then
    rm final-scores.txt
fi


for TEAM in $TEAM_SHORTHANDS; do
    NAME=${TEAM_NAMES[$TEAM]}
    LOGFILE_GZ=($MAP/*$NAME*)
    if [ -f $LOGFILE_GZ ]; then
        if [ ! -d $TEAM ]; then
            echo "unpacking $LOGFILE_GZ ..."
            fn=$(basename $LOGFILE_GZ)
            LOGFILE=$EVALDIR/${fn%.gz}
            if [ ! -f $LOGFILE ]; then
                gunzip -c $LOGFILE_GZ > $LOGFILE
            fi

            mkdir $TEAM
            cd $HOME/$KERNELDIR/boot 
            ./logextract.sh $LOGFILE $EVALDIR/$TEAM
            cd $EVALDIR

            rm $LOGFILE
        fi
        
        cp $TEAM/init-score.txt init-score.txt
        echo -n "\"$NAME\" ">> scores.tmp
        cat $TEAM/scores.txt >> scores.tmp
        echo >> scores.tmp
        echo -n "$TEAM " >> final-scores.txt
        cat $TEAM/final-score.txt >> final-scores.txt
        echo >> final-scores.txt

        for screen in $TEAM/*.png; do
            tn=${screen%.png}-tn.jpg
            convert -format jpg -thumbnail 100x75 -strip -quality 95 PNG8:"$screen" "$tn"
        done
        if [ ! -f snapshot-init.png ]; then
            cp $TEAM/snapshot-init.png snapshot-init.png
            convert -format png -thumbnail 400x300 -strip -quality 95 PNG8:snapshot-init.png snapshot-init-small.png
        fi
#        echo "recompressing ..."
#        bzip2 $LOGFILE
      fi
done

cd $EVALDIR

transpose.py scores.tmp > scores.dat
export RCR_MAP=$MAP
export RCR_COUNT=$(ls -1 $MAP|wc -l)
echo $RCR_COUNT teams processed
gnuplot ../plot.gnu

$DIR/make_html.py $MAP > index.html