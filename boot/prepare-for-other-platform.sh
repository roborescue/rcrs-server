#!/bin/bash
. functions.sh

rm -rf other-platforms/python/*
rm -rf other-platforms/js/*
rm -rf other-platforms/nodejs/*
rm -rf other-platforms/json/*


args="--python_out=other-platforms/python/urn.py --js_out=other-platforms/nodejs/urn.js --json_out=other-platforms/json/urn.json"
makeClasspath $BASEDIR/jars $BASEDIR/lib
java -Xmx1512m -cp $CP:$BASEDIR/jars/rescuecore2.jar:$BASEDIR/jars/standard.jar:$BASEDIR/jars/sample.jar:$BASEDIR/jars/kernel.jar sample.URNMapPrinter $args




./other-platforms/tools/protoc.exe -I=../modules/rescuecore2/src/rescuecore2/messages/protobuf/ --python_out=other-platforms/python/ RCRSProto.proto RCRSLogProto.proto
./other-platforms/tools/protoc.exe -I=../modules/rescuecore2/src/rescuecore2/messages/protobuf/ --js_out=import_style=commonjs:other-platforms/nodejs/ RCRSProto.proto RCRSLogProto.proto

cp other-platforms/nodejs/urn.js other-platforms/js/urn.js


browserify other-platforms/nodejs/RCRSLogProto_pb.js -o other-platforms/js/RCRSLogProto_pb.js
browserify other-platforms/nodejs/RCRSProto_pb.js -o other-platforms/js/RCRSProto_pb.js
