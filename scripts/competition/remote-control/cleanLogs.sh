#!/bin/bash
. $(dirname $0)/config.sh

$(dirname $0)/commandToClients.sh "rm $LOGDIR/* -rf"
