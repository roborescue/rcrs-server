#!/bin/bash
. $(dirname $0)/config.sh

/bin/sh copyToClients.sh $LOCAL_HOMEDIR/$CODEDIR/ $CODEDIR
