#!/bin/bash
. functions.sh



echo "starting gmlEditor...."

makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx512m -cp $CP maps.gml.editor.GMLEditor $1

