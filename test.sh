#!/bin/sh
echo "*******************************************************"
echo "This is provided to run multiple instances of"
echo "Controllers in a hierarchical_control_plane system"
echo "*******************************************************"

echo "*******************************************************"
echo "Usage: ./test.py <parent_controller_root_dir>/ <child_controller1_root_dir>/ <child_controller2_root_dir>/ <mininet_dir>/"
echo "Controllers in a hierachical_control_plane system"
echo "*******************************************************"
if [ "$1" = "--help" ] || [ "$1" = "--?" ]; then
  echo "This script runs the hierarchical_control_plane architecture"
  exit
fi
if [ "$#" -ne 4 ]; then
	echo "Usage: $0 /Dir /Dir /Dir"
	exit 1
fi
if ! [ -d "$1" ] || ! [ -d "$2" ] || ! [ -d "$3" ] || ! [ -d "$4" ]; then
	echo "Exited with 1"
	exit 1
fi 
echo "Reached here"
if [ "$JAVA_HOME" != "" ]; then
	echo "Please set JAVA HOME"
	exit 1
else
	JAVA=$JAVA_HOME/bin/java
fi

cd
HOME_SCRIPT=`pwd`
if [ -d "$1" ]; then
	cd $1
	#if  [-a "start.class"]; then
	#	echo "The Parent Controller directory is not correct"
	#	echo "Please pass the right arguments"
	#	exit 1
	#else
		echo "The Parent controller has started"
		chmod +x "$HOME_SCRIPT/keep.sh"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" java "start"
	#fi
fi
if [ -d "$2" ]; then
	cd $2

		#echo "The child controller 1 path is wrong"
		#echo "Please enter the right directory"
		#exit 1

		echo "The child controller has started"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" "./floodlight.sh"

fi
if [ -d "$3" ]; then
	cd $3
	#if 	[ -e "floodlight.sh" ]; then
		#echo "The child controller 2 path is wrong"
		#echo "Please enter the right directory"
		#exit 1
	#else
		echo "The child controller has started"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" "./floodlight.sh"
	#fi
fi
sleep 5
if [ -d "$4" ]; then
	cd $4
	#if  [ -e "testTopo.py" ]; then
	#	echo "The child controller path is wrong"
	#	echo "Please enter the right directory"
	#	exit 1
	#else
		echo "The network has started"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" sudo "./testTopo.py"
	#fi
fi
echo "close the windows and rerun the script :)"
#"$JAVA" -cp "/home/adeyemi/floodlight/target/bin" "edu/columbia/cs6998/sdn/hw1/Hw1Switch"
#x-terminal-emulator -e "/home/adeyemi/keep.sh" python "$4" 
#x-terminal-emulator -e "/home/adeyemi/keep.sh" "/home/adeyemi/SDN_hw1/floodlight.sh"
