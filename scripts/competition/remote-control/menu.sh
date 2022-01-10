#!/bin/bash

DAY="2010"

processLine(){
  line="$@" # get all args
  #  just echo them, but you may need to customize it according to your need
  # for example, F1 will store first field of $line, see readline2 script
  # for more examples
  # F1=$(echo $line | awk '{ print $1 }')
  echo $line
}

read_file () {
 
### Main script stars here ###
# Store file name
FILE=$1
 
# Make sure we get file name as command line argument
# Else read it from standard input device
if [ "$1" == "" ]; then
   FILE="/dev/stdin"
else
   # make sure file exist and readable
   if [ ! -f $FILE ]; then
  	echo "$FILE : does not exists"
  	exit 1
   elif [ ! -r $FILE ]; then
  	echo "$FILE: can not read"
  	exit 2
   fi
fi
# read $FILE using the file descriptors
 
# Set loop separator to end of line
BAKIFS=$IFS
IFS=$(echo -en "\n\b")
exec 3<&0
exec 0<"$FILE"
while read -r line
do
	# use $line variable to process line in processLine() function
	processLine $line
done
exec 0<&3
 
# restore $IFS which was used to determine what the field separators are
IFS=$BAKIFS
exit 0
}


read_map(){
	read_file
}


read_team_name(){

	OPTIONS="ASP BON BRV ESK FAN MFT MRL POS SBC SDS SEU SOS SMP Exit"
	select team in $OPTIONS; do
		if [ "$team" = "Exit" ]; then
			echo done
			exit
		else
			echo "Team Selected: $team"
			read_day
	       	fi
	done
}

read_file 
