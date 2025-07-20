LOCAL_USER=control
REMOTE_USER=rescue
SOURCEFORGE_USER=mgoe

RECORDSDIR=record-logs
MAPDIR=maps
KERNELDIR=roborescue
CODEDIR=code
SCRIPTDIR=scripts/remote-control
LOGDIR=logs
DISTDIR=logdist
EVALDIR=evaluation
MODE="agent-all"
RSYNC_OPTS=-CE

CLUSTERS="1 2"

HOSTS="c11 c12 c21 c22"
SERVER_HOSTS="c11 c21"
CLIENT_HOSTS="c12 c22"

# HOSTS=localhost
# SERVER_HOSTS=localhost
# CLIENT_HOSTS=localhost

KERNEL_WAITING_TIME=5

PRECOMPUTE_TIMEOUT=125

DAY=final2
YEAR=2025

TEAM_SHORTHANDS="3RA AIT TIM"

declare -A TEAM_NAMES
#TEAM_NAMES[CSU]=CSU-Yunlu
TEAM_NAMES[TIM]=Timrad
TEAM_NAMES[3RA]=3Rakshak
TEAM_NAMES[AIT]=AIT-Rescue


DIR=$(pwd)

declare -A CONNECT_VIEWER
CONNECT_VIEWER[1]=yes
CONNECT_VIEWER[2]=yes
CONNECT_VIEWER[3]=yes


#return hostname of the kernel server of the given cluster
function getServerHost() {
    echo c$11
    # echo 10.10.10.$11
    # echo localhost
}

#return hostnames of the client servers of the given cluster
function getClientHost() {
    local i=$(($2+1))
    echo "c$1$i"
#    echo "c$1-2"
    # echo localhost
}

LOCAL_HOMEDIR=/home/$LOCAL_USER
LOCKFILE_NAME=rsl_run.lock
LOCKFILE_NAME_PRECOMP=/home/$REMOTE_USER/rsl_precomp.lock
STATFILE_NAME=rsl_last_run.stat
