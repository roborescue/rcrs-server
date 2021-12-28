#!/bin/bash

MAP=${MAP:-kobe}
MODE=${RUN_MODE:-NO_PRECOMPUTE}
TEAM=${TEAM:-Test}

case "$MODE" in
      NO_PRECOMPUTE)
        gradle run --args="-c maps/$MAP/config/kernel.cfg --gis.map.dir=maps/$MAP/map --kernel.logname=/logs/rescue.log.xz"
		;;
      PRECOMPUTE)
        gradle run --args="-c maps/$MAP/config/kernel.cfg --gis.map.dir=maps/$MAP/map --kernel.logname=/logs/rescue.log.xz"    
        ;;
	  *)
        echo "Unrecognized option: $MODE"
        exit 1
        ;;
esac