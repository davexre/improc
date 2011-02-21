#include "Arduino.h"
#include "utils.h"
#include "SerialReader.h"

DefineClass(SerialEchoTest);

static SerialReader reader;
static char buf[200];

static void processReader() {
	char *c;
//	Serial.println(reader.bufferFull);
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	Serial.println(c);
}

void SerialEchoTest::setup() {
	reader.initialize(9600, size(buf), buf);
	Serial.println("Done.");
}

void SerialEchoTest::loop() {
	processReader();
	delay(100);
}
