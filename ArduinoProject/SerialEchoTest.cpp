#include "SerialEchoTest.h"

void SerialEchoTest::setup() {
	reader.initialize(9600, size(buf), buf);
	Serial.println("Done.");
}

void SerialEchoTest::processReader() {
	char *c;
	Serial.println(reader.bufferFull);
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	Serial.println(c);
}

void SerialEchoTest::loop() {
	processReader();
	delay(100);
}
