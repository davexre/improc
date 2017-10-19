#!/bin/bash

declare -r TRUE=0
declare -r FALSE=1

##################################################################

function script_home() {
	local SCRIPT_HOME
	SCRIPT_HOME=$(realpath "$0")
	SCRIPT_HOME=$(dirname "$SCRIPT_HOME")
	echo "$SCRIPT_HOME"
}

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
function trim() {
	local f_bak=$-
	set -f
	echo $*
	if [[ $f_bak != *f* ]]; then
		set +f
	fi
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

function onBashErrorHandler() {
	case "${BASH_ERROR_LEVEL}" in
		0 | IGNORE | NONE)
			;;
		1 | WARN)
			echo "Warning in ${1}:${2}. Exit code is ${3} running ${4}"
			;;
		2 | EXIT)
			echo "Error in ${1}:${2}. Exit code is ${3} running ${4}"
			echo "Aborting..."
			exit 1
			;;
	esac
}
BASH_ERROR_LEVEL=EXIT
set -E		# This is needed to have the same trap in sub-shells and shell function
trap 'onBashErrorHandler "${BASH_SOURCE}" "${LINENO}" "$?" "${BASH_COMMAND}"' ERR

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
# Init
##################################################################

log_stdout

false

