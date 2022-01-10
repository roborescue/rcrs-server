#!/bin/bash
. $(dirname $0)/config.sh

$(dirname $0)/"syncClients.sh" $LOCAL_HOMEDIR/$CODEDIR/ $CODEDIR
#/bin/sh "commandToClients.sh" "chmod -R 777 /home/code"
