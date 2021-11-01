LOCAL_USER=rescue
REMOTE_USER=rescue
SOURCEFORGE_USER=mgoe

RECORDSDIR=records-logs
MAPDIR=maps
KERNELDIR=server
CODEDIR=code
SCRIPTDIR=scripts/remote-control
LOGDIR=logs
DISTDIR=logdist
EVALDIR=evaluation

RSYNC_OPTS=-CE

CLUSTERS="1 2"

HOSTS="c1-1 c1-2 c1-3 c1-4 c2-1 c2-2 c2-3 c2-4"
SERVER_HOSTS="c1-1 c2-1"
CLIENT_HOSTS="c1-2 c1-3 c1-4 c2-2 c2-3 c2-4"


# HOSTS=localhost
# SERVER_HOSTS=localhost
# CLIENT_HOSTS=localhost

KERNEL_WAITING_TIME=5

PRECOMPUTE_TIMEOUT=125

DAY=Final
YEAR=2021

TEAM_SHORTHANDS="CSU MRL RI1 AIT"

declare -A TEAM_NAMES
TEAM_NAMES[CSU]=CSU-Yunlu
TEAM_NAMES[MRL]=MRL
TEAM_NAMES[RI1]=Ri-one
TEAM_NAMES[AIT]=AIT-Rescue

DIR=$(pwd)

declare -A CONNECT_VIEWER
CONNECT_VIEWER[1]=no
CONNECT_VIEWER[2]=yes
CONNECT_VIEWER[3]=yes


#return hostname of the kernel server of the given cluster
function getServerHost() {
    echo c$1-1
    # echo 10.10.10.$11
    # echo localhost
}

#return hostnames of the client servers of the given cluster
function getClientHost() {
    local i=$(($2+1))
    echo "c$1-$i"
    # echo localhost
}

LOCAL_HOMEDIR=/home/$LOCAL_USER
LOCKFILE_NAME=rsl_run.lock
LOCKFILE_NAME_PRECOMP=/home/$REMOTE_USER/rsl_precomp.lock
STATFILE_NAME=rsl_last_run.stat
