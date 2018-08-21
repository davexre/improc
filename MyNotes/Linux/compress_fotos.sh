#!/bin/bash

# Create allPictures.txt file
# find /home/slavian/Pictures -type f -printf '%s\t%p\n' > allPictures.txt
#cat allPictures.txt | grep -Evi "(jpg|gif|png|db|zip|xcf|info|kdenlive|odp|txt|7z|tif|odt|3gp|orf|mp4|avi|mov|mpg|wav|thm|wmv)$" | less
#source ~/bin/my_utils.sh

# Tools: mediainfo, ffprobe, identify, mencoder, ffmpeg

declare -A fnames
outdir=./out
bakdir=./bak

mkdir -p "$outdir"
mkdir -p "$bakdir"

function showstat1() {
    if [[ "$fn" == "$p1" ]] ; then
        echo "$i " $(stat -c '%s' "$i")
        echo "$fou " $(stat -c '%s' "$fou")
    fi
}

function showstat2() {
    echo -e $(stat -c '%s' "$fou") "\t" $(stat -c '%s' "$i") "\t" "$fn" "\t" "$i"
}

function play() {
    if [[ "$fn" == "$p1" ]] ; then
        mplayer "$i" &
        mplayer "$fou"
    fi
}

function getinfo() {
    fbak="$bakdir/$fn"
    finfo="${outdir}/${fn}.txt"
	mediainfo "$fbak" > "$finfo"
	#echo $fbak
}

function compress() {
    if [[ -f "$fou" ]] ; then
        echo "Exists $fn"
        return
    fi
    echo "Compress $i"
    mencoder "$i" -ovc x264 -x264encopts preset=slow:profile=main:crf=28 -oac mp3lame -lameopts q=4 -o "$fou" &>> /dev/null
}

function compress2() {
    if [[ -f "$fou" ]] ; then
        return

        echo "Exists $fn"
        size=$(stat -c '%s' "$fou")
        if [[ $size == 0 ]] ; then
            echo "size 0 -> $fn"
            echo "Compress (copy sound) $i"
#            mencoder "$i" -ovc x264 -x264encopts preset=slow:profile=main:crf=28 -oac copy -o "$fou" &>> /dev/null
            mencoder "$i" -ovc x264 -x264encopts preset=slow:profile=main:crf=28 -nosound -o "$fou" &>> /dev/null
        fi
        return
    fi
#    echo "$i"
    echo "Compress $i"
    mencoder "$i" -ovc x264 -x264encopts preset=slow:profile=main:crf=28 -oac mp3lame -lameopts q=4 -o "$fou" &>> /dev/null
}

function checksize() {
    if [[ -f "$fou" ]] ; then
        size=$(stat -c '%s' "$fou")
        if [[ $size == 0 ]] ; then
            echo "size 0 -> $fn"
        fi
    fi
}

function moveMovie() {
    fbak="$bakdir/$fn"
    fdest="$(dirname "$i")/${fn%.*}.avi"
    if [[ -f "$fou" && -f "$i" && ! -f "$fbak" ]] ; then
        mv "$i" "$fbak"
        mv "$fou" "$fdest"
    fi
}

##############################

function imageCompress() {
    if [[ -f "$fou" ]] ; then
        echo "Exists $fn"
        return
    fi
    echo "Compress $i"
	identify -format $'%b\t%w\t%h\t%m\t%x\t%y\t%Q\n' src.jpg
#    mencoder "$i" -ovc x264 -x264encopts preset=slow:profile=main:crf=28 -oac mp3lame -lameopts q=4 -o "$fou" &>> /dev/null
}

function imageGetInfo() {
#	identify -format $'%b\t%w\t%h\t%m\t%x\t%y\t%Q\t%d\t%f\n' "$(readlink -f "$i")"
    stat -c $'%s\t%n' "$i"
}

p1="$1"

#set -e
#cat allPictures.txt | grep -Ei "(3gp)$" | sort -n | cut -f 2 | \
#cat allPictures.txt | grep -Ei "(mp4|avi|mov|mpg|wmv|3gp)$" | sort -n | cut -f 2 | \
#cat allPictures.txt | grep -Ei "(orf|jpg|gif|png|tif)$" | cut -f 2 | \
find bak/ -type f | \
while read i ; do
    fn=$(basename "$i")
    if [[ -n ${fnames[$fn]} ]] ; then
        echo "Duplicate ignored $fn"
        continue
    fi
    fou="$outdir/$fn"

#    showstat1
#    showstat2
#    moveMovie
#    compress
    getinfo
#    checksize
#    play

#	imageGetInfo
#	imageCompress

    if [[ -f stop ]] ; then
        echo "Stopping"
        rm stop
        break
    fi

    fnames[$fn]="$i"
done

notify-send "Done" "Done"

