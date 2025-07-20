#!/bin/bash
. $(dirname $0)/config.sh
export DISPLAY=172.19.0.2:0.0
LOGFILE=$(readlink -f $1)
MAP=$2
TEAM=$3


if [ "$MAP" == "" ];then
    basename="${LOGFILE%.xz}"           # Remove .xz extension
    MAP="${basename##*-}"  
fi
echo MAP is $MAP

if [ -z "$TEAM" ]; then
    for T in $TEAM_SHORTHANDS; do
        NAME="${TEAM_NAMES[$T]}"
        
        if [[ "$LOGFILE" == *"$NAME"* ]]; then
            TEAM="$T"
            break
        fi
    done

    if [ -z "$TEAM" ]; then
        echo "TEAM is invalid"
        exit 1
    fi
fi
echo "TEAM=$TEAM"
MAP_EVALDIR=$HOME/$EVALDIR/$MAP

mkdir -p $MAP_EVALDIR/$TEAM
cd $MAP_EVALDIR

if [[ $LOGFILE == *.tgz ]]; then
    echo "logfile is gzipped: ${LOGFILE%.gz}"
    LOGFILE_GZ=$LOGFILE
    LOGFILE=${LOGFILE%.gz}
    if [ ! -f $LOGFILE ]; then
        gunzip -c $LOGFILE_GZ > $LOGFILE
    fi;

elif [[ $LOGFILE == *.17z ]]; then
    echo "logfile is 7zipped: ${LOGFILE%.7z}"
    LOGFILE_7Z=$LOGFILE
    LOGFILE=${LOGFILE%.7z}

    if [ ! -f $LOGFILE ]; then
        7za e $LOGFILE_7Z
    fi;
fi;


cd $HOME/$KERNELDIR/scripts
echo $HOME/$KERNELDIR/scripts

./logextract.sh $LOGFILE $MAP_EVALDIR/$TEAM
cd $MAP_EVALDIR

for screen in $TEAM/*.png; do
    tn=${screen%.png}-tn.jpg
    convert -format jpg -thumbnail 100x75 -strip -quality 95 PNG8:"$screen" "$tn"
done
if [ ! -f snapshot-init.png ]; then
    cp $TEAM/snapshot-init.png snapshot-init.png
    convert -format png -thumbnail 400x300 -strip -quality 95 PNG8:snapshot-init.png snapshot-init-small.png
fi

echo "Final score is: $(cat $TEAM/final-score.txt)"

echo "Rebuilding summary page for $RUNNING_MAP"

mapSummary.sh $MAP
