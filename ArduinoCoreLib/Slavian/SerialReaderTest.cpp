#include "Arduino.h"
#include "utils.h"
#include "SerialReader.h"

DefineClass(SerialReaderTest);

static SerialReader reader;
static char buf[200];

void SerialReaderTest::setup() {
	reader.initialize(115200, size(buf), buf);
}

void SerialReaderTest::loop() {
	reader.update();
	if (reader.available()) {
		char *c = reader.readln();
		Serial.println(c);
	}
}
