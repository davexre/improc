#!/bin/bash

declare -r TRUE=0
declare -r FALSE=1

LONG_DATE="%Y-%m-%d %H:%M:%S.%N"
COMPACT_DATE="%Y%m%d%H%M%S"
SHORT_DATE="%H:%M:%S"
LOG_DATE="$SHORT_DATE"

SCRIPT_HOME=$(realpath "$0")
SCRIPT_HOME=$(dirname "$SCRIPT_HOME")

LOG_FILE=$(dirname "$0")/$(basename "$0").log
LOG_FILE=$(realpath "$LOG_FILE")

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
# Purpose: Return true if script is executed by the root user
# Arguments: none
# Return: True or False
##################################################################

function is_root() {
	[ $(id -u) -eq 0 ] && return $TRUE || return $FALSE
}
