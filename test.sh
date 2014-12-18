#!/bin/sh
echo "*******************************************************"
echo "This is provided to run multiple instances of"
echo "Controllers in a hierarchical_control_plane system"
echo "*******************************************************"

echo "*******************************************************"
echo "Usage: ./test.sh <parent_controller_root_dir>/ <child_controller1_root_dir>/ <child_controller2_root_dir>/ <mininet_dir>/"
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
echo "Cleaning the network environment"
JAVA=$JAVA_HOME/bin/java


cd
cd hierarchical_control_plane/
HOME_SCRIPT=`pwd`
echo "*********************************************************"
sudo mn -c
echo "*********************************************************"
if [ -d "$1" ]; then
	cd $1
	parent="`pwd`/startTreeController.class"
	echo "Checking $parent if it exists"
	if [ -e "$parent" ]; then
		echo "The Parent backup controller has started"
		chmod +x "$HOME_SCRIPT/keep.sh"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" java "startTreeController" "backup.txt" 
	else
		echo "The Parent Backup Controller directory is not correct"
		echo "Please pass the right arguments"
		exit 1
	fi
	if [ "$?" != 0 ]; then
		echo "Ensure you have the JAVA_HOME set before running script"
		exit 1
	fi
fi
if [ -d "$1" ]; then
	cd $1
	parent="`pwd`/startTreeController.class"
	echo "Checking $parent if it exists"
	if [ -e "$parent" ]; then
		echo "The Parent controller has started"
		chmod +x "$HOME_SCRIPT/keep.sh"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" java "startTreeController" "config.txt"
	else
		echo "The Parent Controller directory is not correct"
		echo "Please pass the right arguments"
		exit 1
	fi
	if [ "$?" != 0 ]; then
		echo "Ensure you have the JAVA_HOME set before running script"
		exit 1
	fi
fi
if [ -d "$2" ]; then
	cd $2
	rm -f "./resources/logbackup.txt" 
	HOME_SCRIPT=`pwd`
	child1="$HOME_SCRIPT/floodlight.sh"
	if [ -e "$child1" ] && [ -e "$HOME_SCRIPT/keep.sh" ]; then
		echo "The child controller 1 has started"
		chmod +x "$HOME_SCRIPT/keep.sh"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" "./floodlight.sh"
	else
		echo "Verify if the child controller 1 path is wrong"
		echo "or keep.sh is in the specified directory"
		exit 1
	fi
fi
if [ -d "$3" ]; then
	cd $3
	rm -f "./resources/logbackup.txt" 
	HOME_SCRIPT=`pwd`
	child2="$HOME_SCRIPT/floodlight.sh"	
	if [ -e "$child2" ] && [ -e "$HOME_SCRIPT/keep.sh" ]; then
		echo "The child controller 2 has started"
		chmod +x "$HOME_SCRIPT/keep.sh"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" "./floodlight.sh"
	else
		echo "Verify that the child controller 2 path is correct"
		echo "or keep.sh is in the specified directory"
		exit 1
	fi
fi
cd
cd hierarchical_control_plane/
HOME_SCRIPT=`pwd`
sleep 5
if [ -d "$4" ]; then
	cd $4
	mnet="`pwd`/testTopo.py"
	if [ -e "$mnet" ] && [ -e "$HOME_SCRIPT/keep.sh" ]; then	
		echo "The network has started"
		x-terminal-emulator -e "$HOME_SCRIPT/keep.sh" sudo "./testTopo.py"
	else
		echo "Verify that the cmininet directory path is correct"
		echo "or keep.sh is in the specified directory"
		exit 1	
	fi
fi
echo "close the windows and rerun the script"
