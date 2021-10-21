#!/bin/bash
. functions.sh

rm -rf platforms/python/*
rm -rf platforms/js/*
rm -rf platforms/nodejs/*
rm -rf platforms/json/*


args="--python_out=platforms/python/URN.py --js_out=platforms/nodejs/URN.js --json_out=platforms/json/URN.json"
makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx1512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar:$BASEDIR/jars/kernel.jar sample.URNMapPrinter $args




./platforms/tools/protoc.exe -I=../modules/rescuecore2/src/rescuecore2/messages/protobuf/ --python_out=platforms/python/ RCRSProto.proto RCRSLogProto.proto
./platforms/tools/protoc.exe -I=../modules/rescuecore2/src/rescuecore2/messages/protobuf/ --js_out=import_style=commonjs:platforms/nodejs/ RCRSProto.proto RCRSLogProto.proto

cp platforms/nodejs/URN.js platforms/js/URN.js


browserify platforms/nodejs/RCRSLogProto_pb.js -o platforms/js/RCRSLogProto_pb.js
browserify platforms/nodejs/RCRSProto_pb.js -o platforms/js/RCRSProto_pb.js
