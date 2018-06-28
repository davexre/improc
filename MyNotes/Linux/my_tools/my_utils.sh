#!/bin/bash
##################################################################
# This file should be sourced from uther scripts:
#	source my_utils.sh
##################################################################

##################################################################
### LOGGING
##################################################################

LONG_DATE="%Y-%m-%d %H:%M:%S.%N"
COMPACT_DATE="%Y%m%d%H%M%S"
SHORT_DATE="%H:%M:%S"
LOG_DATE="$SHORT_DATE"

function log_file() {
	local fn=$(readlink -fm "$1")
	fn=$(default_string "$fn" "$0.log")
	local basefn=$(basename "$fn")
	local noext=${basefn%.*}

	if [[ "$basefn" == "$noext" ]]; then
		fn="${fn}.log"
	fi
	fn=$(readlink -fm "$fn")
	local dir=$(dirname "$fn")
	mkdir -p "$dir"
	LOG_FILE="$fn"
}

function log_stdout() {
	LOG_FILE="/dev/stdout"
}

function log_clear() {
	if [[ -f "$LOG_FILE" ]]; then
		echo -n > "$LOG_FILE"
	fi
}

#-----------------------------------------------------------------
# Log with severity level in first parameter. 
# Severity levels: (see decode_log_level)
#   DEBUG/D/0, INFO/I/1, WARNING/WARN/W/2, ERROR/ERR/E/3, ALL/A/empty
function log_severity() {
	local level=$(decode_log_level "$1")
	local tmp=$(decode_log_level ${LOG_LEVEL})
	if [[ $level -lt $tmp && $tmp -lt 4 ]]; then
		return
	fi
	
	shift
	case $level in
		0)
			tmp="${COLOR[cyan]}DEBUG${COLOR[reset]}"
			;;
		1)
			tmp="${COLOR[blue]}INFO ${COLOR[reset]}"
			;;
		2)
			tmp="${COLOR[yellow]}WARN ${COLOR[reset]}"
			;;
		3)
			tmp="${COLOR[red]}ERROR${COLOR[reset]}"
			;;
		*)
			tmp="     "
			;;
	esac
	
	local timestamp=$(date +"$LOG_DATE")
	echo "${timestamp} ${tmp} $*" >> "$LOG_FILE"
}

function decode_log_level() {
	case "$1" in
		0 | DEBUG | debug | D | d)
			echo 0;
			;;
		1 | INFO | info | I | i)
			echo 1;
			;;
		2 | WARNING | warning | WARN | warn | W | w)
			echo 2
			;;
		3 | ERROR | error | ERR | err | E | e)
			echo 3
			;;
		4 | ALL | all | A | a | *)
			echo 4;
			;;
	esac
}

function log() {
	log_severity "" "$*"
}

function log_debug() {
	log_severity "DEBUG" "$*"
}

function log_warn() {
	log_severity "WARN" "$*"
}

function log_info() {
	log_severity "INFO" "$*"
}

function log_error() {
	log_severity "ERROR" "$*"
}

#-----------------------------------------------------------------
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
	echo -n "$@" | sed -ze 's/^[[:space:]]*//g; s/[[:space:]]*$//g'
}

#-----------------------------------------------------------------
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
	local exitCode=0
	([[ "$1" != "" ]] && "$@" >& /dev/null) || exitCode=$?
	#return $exitCode
}

#-----------------------------------------------------------------
# Purpose: Returns exit code 0 if script is executed by the root user
# Return: exit code 0 -> root, 1->not root
# Usage: is_root && echo root || echo not root
function is_root() {
	[[ $(id -u) -eq 0 ]] && return 1 || return 0
}

#-----------------------------------------------------------------
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
### Bash file processing/configuration at the end of script
##################################################################

#-----------------------------------------------------------------
# Searches the $0 file for marker $1 and returns the line number.
# Usage: local myTokenAtLine=$(findToken "___SOME_MARKER___")
# The script should contain the token ___SOME_MARKER___ somewhere 
# at the end of the file. Remember to put an "exit" as last shell command
# prior the token.
function findToken() {
	grep -anm 1 "^${1}\$" "$0" | cut -d ':' -f 1
}

#-----------------------------------------------------------------
# Extracts a data segment in $0.
# Usage: 
#	local myData=$(extractData "___DATA_1_MARKER___")
#	local myData=$(extractData "___DATA_1_MARKER___" "___DATA_2_MARKER___")
# The shorter version:
# function extractData() {
# 	local begin=$(findToken "$1")
# 	local end=${2:+$(findToken "$2")}
# 	end=${end:+$(($end-$begin-1))}
# 	(tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$') || true
# }
function extractData0() {
	if [[ -z "$1" ]]; then
		return
	fi
	local begin=$(findToken "$1")
	if [[ -z $begin ]]; then
		return
	fi
	local end="${2}"
	if [[ -z $end ]]; then
		tail -n +$(($begin+1)) "$0"
	else
		end=$(findToken "$end")
		if [[ -z $end ]]; then
			return
		fi
		end=${end:+$(($end-$begin-1))}
		(tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"}) || true
	fi
}

#-----------------------------------------------------------------
# Extracts a data segment in $0. Removes any empty lines or line beginning with #.
# Usage: 
#	local myData=$(extractData "___DATA_1_MARKER___")
#	local myData=$(extractData "___DATA_1_MARKER___" "___DATA_2_MARKER___")
# The shorter version:
# function extractData() {
# 	local begin=$(findToken "$1")
# 	local end=${2:+$(findToken "$2")}
# 	end=${end:+$(($end-$begin-1))}
# 	(tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$') || true
# }
function extractData() {
	if [[ -z "$1" ]]; then
		return
	fi
	local begin=$(findToken "$1")
	if [[ -z $begin ]]; then
		return
	fi
	local end="${2}"
	if [[ -z $end ]]; then
		tail -n +$(($begin+1)) "$0" | grep -v '^\s*\(#.*\)\?$'
	else
		end=$(findToken "$end")
		if [[ -z $end ]]; then
			return
		fi
		end=${end:+$(($end-$begin-1))}
		(tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$') || true
	fi
}

#-----------------------------------------------------------------
# Extracts a data segment in $0.
# Usage: 
#	local myDataLoc1=$(findToken "___DATA_1_MARKER___")
#	local myDataLoc2=$(findToken "___DATA_2_MARKER___")
#	local myData=$(extractDataLines $myDataLoc1)
#	local myData=$(extractDataLines $myDataLoc1 $myDataLoc2)
function extractDataLines() {
	local begin="$1"
	if [[ -z $begin ]]; then
		return
	fi
	local end=${2:+$(($2-$begin-1))}
	(tail -n +$(($begin+1)) "$0" | head -n ${end:-"-0"} | grep -v '^\s*\(#.*\)\?$') || true
}

#-----------------------------------------------------------------
# Modifies the current script by replacing a data segment. See extractData
# Usage:
#	myData=$(echo -e "my multi-\nline data")
#	replaceData "$myData" "___DATA_1_MARKER___"
#	replaceData "$myData" "___DATA_1_MARKER___" "___DATA_2_MARKER___"
function replaceData() {
	local script=$(
		local begin=$(findToken "$2")
		local end=$(findToken "$3")
		if [[ (-z "$begin") || (! -z "$3" && -z "$end") ]]; then
			return
		fi
		head -n $begin "$0"
		echo "$1"
		if [[ -n $3 ]]; then
			tail -n "+$end" "$0"
		fi
	)
	if [[ -z "$script" ]]; then
		log_error "Error saving data to script"
		return 1
	fi
	echo "$script" > "$0"
}

#-----------------------------------------------------------------
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
	local keys vals v
	local DATA=$(extractData "$1" "$2")
	readarray -t keys < <(echo "$DATA" | cut -d '=' -f 1)
	readarray -t vals < <(echo "$DATA" | cut -d '=' -f 2-)
	if [[ ! -z "${keys[@]}" ]]; then
		for i in "${!keys[@]}" ; do
			v=$(echo -e "${vals[$i]}")
			cfg[$(trim "${keys[$i]}")]=$(trim "$v")
		done
	fi
}

#-----------------------------------------------------------------
# Save the data in the global variable (associative array) called cfg.
# See read_cfg.
# Usage:
#	save_cfg "___DATA_1_MARKER___"
#	save_cfg "___DATA_1_MARKER___" "___DATA_2_MARKER___"
function save_cfg() {
	local cfg_data=$(
		for i in "${!cfg[@]}" ; do
			#local v=$(echo -n "${cfg[$i]}" | sed -rze 's/(\r)|(\n)|(\r\n)|(\n\r)/\\n/g')
			#local v=$(printf '%q' "${cfg[$i]}" | sed -re 's/^\$?'"'(.*)'"'$/\1/; s/\\([][$\ ])/\1/')
			local v=$(printf '%q' "${cfg[$i]}" | sed -re 's/^\$?'"'(.*)'"'$/\1/; s/\\([][$\'"'"' ])/\1/')
			echo "$i = $v"
		done
	)
	replaceData "$cfg_data" "$1" "$2"
}

#-----------------------------------------------------------------
# Evaluates all variables in $1.
# Usage:
#	my_variable='some string'
#	str=$'some\nmultiline ${my_variable} that\nshould be evaluated'
#	result=$(evalVariables "$str")
# WARNING: If the string contains executable statement it will be executed!
#	ex: my_var=$(evalVariables "just created $(mkdir -p ~/temp/a) a folder")
function evalVariables() {
	eval echo -E "\"$(sed -e 's/\\/\\\\/g' -e 's/"/\\"/g' <<< "$1")\""
}

#-----------------------------------------------------------------
# Returns the bash call stack
# Usage: 
#	getStack [number_of_items_to_skip_default_1]
function getStack() {
	local start
	start=${1:-1}
	for ((i=$start; $i<${#FUNCNAME[@]}; i++)) ; do
		echo ${BASH_SOURCE[$i]}:${BASH_LINENO[$i]}:${FUNCNAME[$i]}
	done
}

#-----------------------------------------------------------------
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
			getStack 2
			echo "Aborting..."
			exit 255
			;;
	esac
}

#-----------------------------------------------------------------
# Set bash option (shopt) and return it's previous value.
# Usage: 
#	local myBak=$(setBashOption nocasematch 1)
#	setBashOption nocasematch $myBak
function setBashOption() {
	local val bak='1'
	shopt -q $1 || bak='0'
	case $2 in 
		1 | Y | y | yes | YES | T | TRUE | t | true | -s)
			val='-s'
			;;
		*)
			val='-u'
			;;
	esac
	shopt $val $1
	echo $bak
}

#-----------------------------------------------------------------
# Enable/disable colors
# Usage: 1-> enable colors 0->disable
#	colors 1 
function colors() {
	declare -Ag COLOR
	# To make new escape codes use: printf "%q\n" "$(tput setaf 1)"
	# Use "man tput" and "man terminfo" to get parameters for tput
	COLOR[black]=$'\E[30m'
	COLOR[red]=$'\E[31m'
	COLOR[green]=$'\E[32m'
	COLOR[yellow]=$'\E[33m'
	COLOR[blue]=$'\E[34m'
	COLOR[magenta]=$'\E[35m'
	COLOR[cyan]=$'\E[36m'
	COLOR[white]=$'\E[37m'
	COLOR[reset]=$'\E(B\E[m'
	
	COLOR[0]="${COLOR[reset]}"
	COLOR[1]="${COLOR[red]}"
	COLOR[2]="${COLOR[green]}"
	COLOR[3]="${COLOR[yellow]}"
	COLOR[4]="${COLOR[blue]}"
	COLOR[5]="${COLOR[magenta]}"
	COLOR[6]="${COLOR[cyan]}"
	COLOR[7]="${COLOR[white]}"
	COLOR[8]="${COLOR[black]}"

	case $1 in 
		1 | Y | y | yes | YES | T | TRUE | t | true | -s)
			;;
		*)
			for i in "${!COLOR[@]}" ; do
				COLOR["$i"]=''
			done
			;;
	esac
}

##################################################################
### Init
##################################################################

set -E
BASH_ERROR_LEVEL=EXIT
trap 'onBashErrorHandler "${BASH_SOURCE}" "${LINENO}" "$?" "${BASH_COMMAND}"' ERR

colors YES
LOG_LEVEL=ALL;
log_stdout

SCRIPT_HOME=$(dirname "$(readlink -f "${BASH_SOURCE[ ${#BASH_SOURCE[@]}-1 ]}")")
