#!/bin/bash
. $(dirname $0)/config.sh

$(dirname $0)/"syncKernels.sh" $LOCAL_HOMEDIR/$MAPDIR/ $MAPDIR
