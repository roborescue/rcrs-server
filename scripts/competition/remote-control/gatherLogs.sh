#!/bin/bash
. $(dirname $0)/config.sh

$(dirname $0)/gatherFromClients.sh $LOGDIR $HOME
$(dirname $0)/gatherFromKernels.sh $LOGDIR $HOME
