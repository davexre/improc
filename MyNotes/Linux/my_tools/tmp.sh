#!/bin/bash

source my_utils.sh

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
