#!/usr/bin/env python
import datetime
import fileinput
import re
import subprocess

pid = 0
command = ""
fileDescriptor = ""
#fnamePat = re.compile('/home/spetrov.*')
#fnamePat = re.compile('.*\.jar$')
fnamePat = re.compile('/home/spetrov/.*TMP.html.gz')

count = 0
#for line in fileinput.input():
proc = subprocess.Popen(args=["lsof", "-F", "pcn", "+r", "5", "+d", "/home/spetrov/"], stdout=subprocess.PIPE)
#proc = subprocess.Popen(args=["cat", "/home/spetrov/temp/aa"], stdout=subprocess.PIPE)
#proc = subprocess.Popen(args=["ls", "-la"], stdout=subprocess.PIPE)
for line in proc.stdout:
	count = count + 1
	line = line.rstrip('\r\n')
	if len(line) > 0:
		cmd = line[0]
		line = line[1:]
		if cmd == "p":
			pid = line
		elif cmd == "c":
			command = line
		elif cmd == "f":
			fileDescriptor = line
		elif cmd == "n":
			if fnamePat.match(line):
				stamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
				print(stamp + "\t" + pid + "\t" + command + "\t"+ line)
