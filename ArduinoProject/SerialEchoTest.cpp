//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "SerialReader.h"

char buf[200];

extern "C" void setup() {
	reader.initialize(9600, size(buf), buf);
	Serial.println("Done.");
}

void processReader() {
	char *c;
	Serial.println(reader.bufferFull);
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	Serial.println(c);
}

extern "C" void loop() {
	processReader();
	delay(100);
}

#endif
