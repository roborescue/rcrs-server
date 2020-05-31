#!/bin/bash

cd `dirname $0`

. utils.sh
. functions.sh

LOGDIR='logs'
MAP_DIR=$1

if [[ ! -z "$MAP_DIR" ]]; then
    MAP_DIR=$(abspath $MAP_DIR)

    if [[ -z "$MAP_DIR" ]]; then
        exit 1
    fi
fi

echo "starting gmlEditor...."

makeClasspath $BASEDIR/jars $BASEDIR/lib
execute gml-editor "java -Xmx512m -cp $CP -Dlog4j.log.dir=$LOGDIR maps.gml.editor.GMLEditor $MAP_DIR"
