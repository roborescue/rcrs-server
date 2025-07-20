#!/bin/bash

set -euo pipefail
cd "$(dirname "$0")"
team=$1
UPLOAD_DIR="$2"
echo "🚀 Starting compilation for: $UPLOAD_DIR"

# 🔍 Find valid project root (with src/ and config/)
find_project_root() {
    if [[ -d "$UPLOAD_DIR/src" && -d "$UPLOAD_DIR/config" ]]; then
        echo "$UPLOAD_DIR"
        return
    fi

    for sub in "$UPLOAD_DIR"/*/; do
        if [[ -d "${sub}/src" && -d "${sub}/config" ]]; then
            echo "${sub%/}"
            return
        fi
    done

    echo "❌ Error: No valid project root with both src/ and config/ found." >&2
    exit 1
}

code="$(find_project_root)"
echo "📁 Project root resolved to: $code"

# ✅ Run your compile logic
compile() {
    echo "🔧 Compiling team $team in: $code"

compile=$code-compile
good_compile=/opt/codes/$team/
history=/opt/history/$team/$(date '+%Y-%m-%d_%H-%M-%S')
rm -rf $compile
mkdir $compile
export GRADLE_USER_HOME="/tmp/$team/.gradle"
mkdir -p $GRADLE_USER_HOME
  echo team=$team code=$code

  if [[ ! -d "$code" ]];then
    echo "invalid code"
    exit 2
fi
    TEAM_SRC_DIR="$code"
    TEAM_DST_DIR="$compile"
    cp -R adf-sample-agent-java/* $TEAM_DST_DIR
    cp -R $TEAM_SRC_DIR/data/* $TEAM_DST_DIR/data
    cp $TEAM_SRC_DIR/config/module.cfg $TEAM_DST_DIR/config
    cp -R $TEAM_SRC_DIR/lib*/* $TEAM_DST_DIR/lib
    mkdir -p $TEAM_DST_DIR/src/main/java

    cp -R $TEAM_SRC_DIR/src/main/java/* $TEAM_DST_DIR/src/main/java

    sed -i s/sample_team/$team/g $TEAM_DST_DIR/config/launch.cfg

    cd $TEAM_DST_DIR
    ./gradlew clean build --no-daemon  --gradle-user-home=$GRADLE_USER_HOME
    if [ $? == 0 ]; then
       rm -rf $good_compile
       cp -r . $good_compile
       echo "code successfully compiled and copied."
       mkdir -p $history
       cp -r $TEAM_SRC_DIR/* $history
    else
      echo "code compile failed! the previous code won't be replaced!"
      exit 1
    fi


}

compile
exit $?
