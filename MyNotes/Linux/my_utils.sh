#!/bin/bash
##################################################################
# This file should be sourced from uther scripts:
#	source my_utils.sh

##################################################################
### LOGGING
##################################################################

LONG_DATE="%Y-%m-%d %H:%M:%S.%N"
COMPACT_DATE="%Y%m%d%H%M%S"
SHORT_DATE="%H:%M:%S"
LOG_DATE="$SHORT_DATE"

function log_file() {
	local fn=$(realpath -q "$1")
	fn=$(default_string "$fn" "$0.log")
	local basefn=$(basename "$fn")
	local noext=${basefn%.*}

	if [[ "$basefn" == "$noext" ]]; then
		fn="${fn}.log"
	fi
	fn=$(realpath -mq "$fn")
	LOG_FILE="$fn"
}

function log_stdout() {
	LOG_FILE="/dev/stdout"
}

function log_clear() {
	local tmp=$(dirname "$LOG_FILE")
	if [[ "$tmp" != "/dev" ]]; then
		rm -f "$LOG_FILE"
		touch "$LOG_FILE"
	fi
}
function log_severity() {
	local severity="$1"
	shift
	local timestamp=$(date +"$LOG_DATE")
	echo "${timestamp}${severity} $*" >> "$LOG_FILE"
}

function log() {
	log_severity "" "$*"
}

function log_debug() {
	log_severity " DEBUG" "$*"
}

function log_warn() {
	log_severity " WARN " "$*"
}

function log_info() {
	log_severity " INFO " "$*"
}

function log_error() {
	log_severity " ERROR" "$*"
}

##################################################################
# STRING Processing
function trim2() {
	local f_bak=$-
	set -f
	echo $@
	if [[ $f_bak != *f* ]]; then
		set +f
	fi
}

function trim() {
	echo "$@" | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//'
}

##################################################################
# Usage: local myVar=$(default_string "$2" "$1" "$@" "Some default value")
function default_string() {
	while [ $# -gt 0 ]; do
		if [[ ! -z "$1" ]]; then
			echo "$1"
			return
		fi
		shift
	done
}

##################################################################

function execQuiet() {
	[[ "$1" != "" ]] && "$@" >& /dev/null
}

function makeError() {
	false
}

##################################################################
# Purpose: Return true if script is executed by the root user
# Arguments: none
# Return: True or False
function is_root() {
	[ $(id -u) -eq 0 ] && return $TRUE || return $FALSE
}

##################################################################
# Copy current user ssh key to a host
# $1=username@host $2=password
function copy_ssh_id() {
	sshpass -p "${2}" ssh-copy-id -o StrictHostKeyChecking=no "${1}"
}

function copy_ssh_id_OLD() {
expect << EOF
	spawn ssh-copy-id "${1}"
	expect {
		"password:" {
			send "${2}\r"
			expect "were added."
		} "already exist on the remote system." {
		} eof {
			exit 1
		}
	}
EOF
}

##################################################################
### Bash file processing/configuration at the end of scrip
##################################################################

##################################################################
# Searches the $0 file for marker $1 and returns the line number.
# Usage: local myTokenAtLine=$(findToken "___SOME_MARKER___")
# The script should contain the token ___SOME_MARKER___ somewhere 
# at the end of the file. Remember to put an "exit" as last shell command
# prior the token.
function findToken() {
	grep -anm 1 "^${1}\$" "$0" | cut -d ':' -f 1
}

##################################################################
# Extracts a data segment in $0.
# Usage: 
#	local myData=$(extractData "___DATA_1_MARKER___")
#	local myData=$(extractData "___DATA_1_MARKER___" "___DATA_2_MARKER___")
function extractData() {
	local begin=$(findToken "$1")
	local end=${2:+$(findToken "$2")}
	end=${end:+$(($end-$begin-1))}
	tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$'
}

##################################################################
# Extracts a data segment in $0.
# Usage: 
#	local myDataLoc1=$(findToken "___DATA_1_MARKER___")
#	local myDataLoc2=$(findToken "___DATA_2_MARKER___")
#	local myData=$(extractDataLines $myDataLoc1)
#	local myData=$(extractDataLines $myDataLoc1 $myDataLoc2)
function extractDataLines() {
	local begin="$1"
	local end=${2:+$(($2-$begin-1))}
	tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$'
}

##################################################################
# Modifies the current script by replacing a data segment. See extractData
# Usage:
#	myData=$(echo -e "my multi-\nline data")
#	replaceData "$myData" "___DATA_1_MARKER___"
#	replaceData "$myData" "___DATA_1_MARKER___" "___DATA_2_MARKER___"
function replaceData() {
	local script=$(
		local begin=$(findToken "$2")
		head -n $begin "$0"
		echo "$1"
		if [[ -n $3 ]]; then
			tail -n +$(findToken "$3") "$0"
		fi
	)
	echo "$script" > "$0"
}

##################################################################
# Property files-like configuration located in a data segment of 
# the script. The configuration is read in a global variable called
# cfg. The variable is associative array. Individual properties 
# can be accessed by "${cfg[myPropertyKey]}". Both property keys
# and values are trimmes. See extractData.
# Usage:
#	read_cfg "___DATA_1_MARKER___"
#	read_cfg "___DATA_1_MARKER___" "___DATA_2_MARKER___"
function read_cfg() {
	unset cfg
	declare -Ag cfg
	local keys vals
	local DATA=$(extractData "$1" "$2")
	readarray -t keys < <(echo "$DATA" | cut -d '=' -f 1)
	readarray -t vals < <(echo "$DATA" | cut -d '=' -f 2-)
	for i in "${!keys[@]}" ; do
		cfg[$(trim "${keys[$i]}")]=$(trim $(echo -e "${vals[$i]}"))
	done
}

##################################################################
# Save the data in the global variable (associative array) called cfg.
# See read_cfg.
# Usage:
#	save_cfg "___DATA_1_MARKER___"
#	save_cfg "___DATA_1_MARKER___" "___DATA_2_MARKER___"
function save_cfg() {
	local cfg_data=$(
		for i in "${!cfg[@]}" ; do
			echo "$i=${cfg[$i]}"
		done
	)
	replaceData "$cfg_data" "$1" "$2"
}

##################################################################
# Error handling in bash
# Usage: 
#	BASH_ERROR_LEVEL=IGNORE
#	set -E	# This is needed to have the same trap in sub-shells and shell function
#	set -u	# This is to trap unset parameters/variables as errors
#	trap 'onBashErrorHandler "${BASH_SOURCE}" "${LINENO}" "$?" "${BASH_COMMAND}"' ERR
function onBashErrorHandler() {
	case "${BASH_ERROR_LEVEL}" in
		0 | IGNORE | NONE)
			;;
		1 | WARN)
			echo "Warning in ${1}:${2}. Exit code is ${3} running ${4}"
			;;
		2 | EXIT | *)
			echo "Error in ${1}:${2}. Exit code is ${3} running ${4}"
			echo "Aborting..."
			exit 255
			;;
	esac
}

##################################################################
# Init
##################################################################

set -Eu
BASH_ERROR_LEVEL=EXIT
trap 'onBashErrorHandler "${BASH_SOURCE}" "${LINENO}" "$?" "${BASH_COMMAND}"' ERR
declare -r TRUE=0
declare -r FALSE=1
log_stdout

SCRIPT_HOME=$(realpath "$0")
SCRIPT_HOME=$(dirname "$SCRIPT_HOME")
