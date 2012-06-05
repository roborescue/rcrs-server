LOCAL_USER=goebelbe
REMOTE_USER=rescue

MAPDIR=maps
KERNELDIR=rescue-1.1-dutchopen2012
CODEDIR=code
SCRIPTDIR=scripts
LOGDIR=logs
DISTDIR=logdist
EVALDIR=evaluation

RSYNC_OPTS=-C

CLUSTERS="1 2"

HOSTS="c1-1 c1-2 c1-3 c1-4 c2-1 c2-2 c2-3 c2-4 rescue-control"
SERVER_HOSTS="c1-1 c2-1"
CLIENT_HOSTS="c1-2 c1-3 c1-4 c2-2 c2-3 c2-4"

# HOSTS=localhost
# SERVER_HOSTS=localhost
# CLIENT_HOSTS=localhost

KERNEL_WAITING_TIME=5

DAY=final

TEAM_SHORTHANDS="BAS FCP KAV"

declare -A TEAM_NAMES
TEAM_NAMES[BAS]=Baseline
TEAM_NAMES[FCP]=FC-Portugal
TEAM_NAMES[KAV]=Kaveh

DIR=$(pwd)

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
STATFILE_NAME=rsl_last_run.stat