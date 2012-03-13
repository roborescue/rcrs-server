LOCAL_USER=goebelbe
REMOTE_USER=rsl2011

MAPDIR=maps
KERNELDIR=src/roborescue
CODEDIR=code
SCRIPTDIR=scripts
LOGDIR=logs
DISTDIR=logdist

HOSTS="c1-1 c1-2 c1-3 c1-4 c2-1 c2-2 c2-3 c2-4 c3-1 c3-2 c3-3 c3-4"
SERVER_HOSTS="c1-1 c2-1 c3-1"
CLIENT_HOSTS="c1-2 c1-3 c1-4 c2-2 c2-3 c2-4 c3-2 c3-3 c3-4"

# HOSTS=localhost
# SERVER_HOSTS=localhost
# CLIENT_HOSTS=localhost

KERNEL_WAITING_TIME=5

DAY=final

TEAM_SHORTHANDS="RAK HER SBC POS LTI EPI IAM MRL RI1 SEU BON SUN RMA NAI ANC BRC"

declare -A TEAM_NAMES
TEAM_NAMES[RAK]=RoboAKUT
TEAM_NAMES[HER]=HfutEngineRescue
TEAM_NAMES[SBC]=SBCe_Saviour
TEAM_NAMES[POS]=Poseidon
TEAM_NAMES[LTI]=LTI_Agent_Rescue
TEAM_NAMES[EPI]=epicenter
TEAM_NAMES[IAM]=IAMRescue
TEAM_NAMES[MRL]=MRL
TEAM_NAMES[RI1]=Ri-one
TEAM_NAMES[SEU]=SEU_RedSun
TEAM_NAMES[BON]=BonabRescue
TEAM_NAMES[SUN]=SUNTORI
TEAM_NAMES[RMA]=RMAS_ArtSapience
TEAM_NAMES[NAI]=Naito_Rescue_2011
TEAM_NAMES[ANC]=anct_resq_2011
TEAM_NAMES[BRC]=Brave_Circles


#return hostname of the kernel server of the given cluster
function getServerHost() {
    # echo c$1-1
    echo 10.10.10.$11
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
DIR=$(pwd)