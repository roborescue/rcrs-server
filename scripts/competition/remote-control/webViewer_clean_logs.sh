#! /bin/bash

RECORDS_DIR=~/records-logs

days="Day1 Day2 Semifinal Final"
maps="kobe-test vc1 joao1 berlin1 eindhoven1 mexico1 ny1 kobe1 sydney1 sakaei1 paris1 vc2 berlin2 eindhoven2 istanbul1 sf1 ny2 paris2 kobe2 montreal1 sf2 sydney2 berlin3 kobe3 sakae2 paris3"

for day in $days; do
  for map in $maps; do

  DAY_NAME=$day
  MAP_NAME=$map

  CURRENT=$(pwd)
  cd $RECORDS_DIR/$DAY_NAME
  pwd
    for d in */ ;do
      echo "-------- $d";
      LAST_LOG=$(ls -1 $d$MAP_NAME* | tail -1)
      arrIN=(${LAST_LOG//// })
      LAST_LOG_NAME=${arrIN[1]}
      ls $d$MAP_NAME* | grep -v $LAST_LOG_NAME | xargs -I % sh -c "echo %; rm %";
    done
  done
done
