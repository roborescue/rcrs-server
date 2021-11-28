#!/bin/bash

## Software
##   Node.js
##   NPM
##   Google Protobuf
##
## Require JS libraries
##   browserify
##   google-protobuf
##

. ../functions.sh

## Remove existing protobuf stubs
rm -rf python/*
rm -rf js/*
rm -rf nodejs/*
rm -rf json/*

## Generate URN Mapping
makeClasspath ../../jars ../../lib
java -Xmx1512m -cp $CP rescuecore2.standard.misc.URNMapPrinter --python_out=python/URN.py --js_out=nodejs/URN.js --json_out=json/URN.json

## Java
protoc -I=../../modules/rescuecore2/src/rescuecore2/messages/protobuf --java_out=../../modules/rescuecore2/src RCRSProto.proto RCRSLogProto.proto

## Python
protoc -I=../../modules/rescuecore2/src/rescuecore2/messages/protobuf --python_out=python RCRSProto.proto RCRSLogProto.proto

## Node.js
protoc -I=../../modules/rescuecore2/src/rescuecore2/messages/protobuf --js_out=import_style=commonjs:nodejs/ RCRSProto.proto RCRSLogProto.proto

## JavaScript
cp nodejs/URN.js js/URN.js

npm install --save browserify google-protobuf

./node_modules/.bin/browserify nodejs/RCRSLogProto_pb.js -o js/RCRSLogProto_pb.js
./node_modules/.bin/browserify nodejs/RCRSProto_pb.js -o js/RCRSProto_pb.js