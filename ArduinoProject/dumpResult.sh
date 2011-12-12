#!/bin/bash
#
# Post-build step command:
# ../dumpResult.sh ${BuildArtifactFileBaseName}
#
fin=$1.elf
fou=$1.lss2
echo Souce file is ${fin}
echo Generating output file ${fou} 

echo ... > ${fou}
avr-size --format=avr --mcu=atmega328p ${fin} >> ${fou}
echo ... >> ${fou}
echo ... >> ${fou}
avr-size --format=Berkeley --mcu=atmega328p ${fin} >> ${fou}
echo ... >> ${fou}
echo ... >> ${fou}
avr-objdump -s -j .data ${fin} >> ${fou}
echo ... >> ${fou}
echo ... >> ${fou}
echo "Print final symbols sizes (b - .bss; d - .data; t - .text; v/w - weak)" >> ${fou}
echo ... >> ${fou}
echo ... >> ${fou}
avr-nm -td --size-sort -C ${fin}|sort -f -k 2,2 -k 1,1 >> ${fou}
echo ... >> ${fou}
echo ... >> ${fou}
avr-objdump -h -C -S ${fin} >> ${fou}
 