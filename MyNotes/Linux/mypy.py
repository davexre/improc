#!/usr/bin/env python
import fileinput
count = 0
for line in fileinput.input():
	line = line.rstrip('\r\n')
	count = count + 1
	print(str(count) + ": " + line)