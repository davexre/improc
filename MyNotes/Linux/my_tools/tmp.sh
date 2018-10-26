#!/bin/bash

source my_utils.sh

BASEDIR=/home/slavian/Pictures
DIR=/home/slavian/temp/a
OUT=$DIR/img/tmp

function makeImgList() {
	sqlite3 "$DIR/img_jpeg_info.sqlite" <<- EOF
		select substr(fname, length('$BASEDIR/') + 1)
		from img
		where 
			quality > 65 and
			fname like '$BASEDIR/20%'
		order by fname desc;
EOF
}

function processOne() {
	fin=$BASEDIR/$1
	fou=$OUT/$1
	if [[ -f "$fou" || ! -f "$fin" ]] ; then
		return
	fi
	echo $1
	base=$(dirname "$fou")
	mkdir -p "$base"
	convert "$fin" -compress JPEG -quality 65 "$fou"
}

function checkOne() {
	fin=$BASEDIR/$1
	fou=$OUT/$1
	if [[ ! -f "$fou" || ! -f "$fin" ]] ; then
		return
	fi
	sin=$(stat -c '%s' "$fin")
	sou=$(stat -c '%s' "$fou")
	rat=$(( 100*$sou/$sin ))
	if [[ $rat > 70 ]] ; then
		return
	fi
	echo -e "$rat\t$sin\t$sou\t$fin"
}

makeImgList | while read i ; do
	checkOne "$i"
done

exit 0

asd='qwe 
${var} aaa "
zxc'

var="Some value"

echo "${asd}"

echo
#asd2=$(sed 's/"/\\"/g' <<< "$asd")
eval echo "\"$(sed 's/"/\\"/g' <<< "$asd")\""
my_var=$(evalVariables "evaluate just created $(mkdir -p ~/temp/a) a folder")
echo $my_var

#zxc=$(eval "${asd}")
#echo "${zxc}"
