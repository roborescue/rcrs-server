#!/bin/bash 

xhost +local:*
docker run -it --rm -p 27931:27931 -e DISPLAY=$DISPLAY \
--env map=$1 \
--env precompute=True \
--env rcrs_path=$(pwd)/.. \
-v /tmp/.X11-unix:/tmp/.X11-unix \
--mount type=bind,source="$(pwd)"/../logs,target=/rcrs_server/logs \
--mount type=bind,source="$(pwd)"/../maps,target=/rcrs_server/maps \
--net=host \
rcrs 
