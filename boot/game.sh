#! /bin/sh

if [ $# -ne 2 ]; then
    echo "usage: game.sh <map name> <team name>"
    echo "e.g.: game.sh Kobe MyRescue"
    exit 1
fi

`dirname $0`/all.sh $*

