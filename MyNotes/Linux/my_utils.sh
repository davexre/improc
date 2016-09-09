#!/bin/bash

declare -r TRUE=0
declare -r FALSE=1

function script_home() {
	local SCRIPT_HOME
	SCRIPT_HOME=$(realpath "$0")
	SCRIPT_HOME=$(dirname "$SCRIPT_HOME")
	echo "$SCRIPT_HOME"
}

### LOGGING

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

### STRING Processing

function trim() {
	set -f
	echo $*
}

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

function execQuiet() {
	[[ "$1" != "" ]] && "$@" >& /dev/null
}

function makeError() {
	false
}

function log_trapped_error() {
	log_error "Error in $1:$2:$3. Exit code is $4 running \"$5\""
}

function trap_errors() {
	trap 'log_trapped_error "${BASH_SOURCE}" "${FUNCNAME}" "${LINENO}" "$?" "${BASH_COMMAND}"' ERR
}

function untrap_errors() {
	trap -- ERR
}

##################################################################
# Purpose: Return true if script is executed by the root user
# Arguments: none
# Return: True or False
##################################################################

function is_root() {
	[ $(id -u) -eq 0 ] && return $TRUE || return $FALSE
}

##################################################################
# Init
##################################################################

log_stdout

false

