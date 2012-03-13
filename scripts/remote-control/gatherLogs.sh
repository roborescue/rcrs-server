#!/bin/bash
. $(dirname $0)/config.sh

$(dirname $0)/gatherFromClients.sh $LOGDIR .
$(dirname $0)/gatherFromKernels.sh $LOGDIR .


