#!/bin/bash

mkdir -p tmp
rm -rf ./tmp/*
rsync -rvlc --info=progress2 --info=name0 --exclude=../docker ../ ./tmp/
docker build . -t rcrs
rm -rf ./tmp/*
