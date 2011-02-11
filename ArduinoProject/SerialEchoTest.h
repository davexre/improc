#ifndef SerialEchoTest_h
#define SerialEchoTest_h

#include <WProgram.h>
#include "utils.h"
#include "SerialReader.h"

class Base {
public:
	void setup() {};
	void loop() {};
};

class SerialEchoTest : Base {
public:
	SerialReader reader;
	char buf[200];
	void setup();
	void processReader();
	void loop();
};

#endif
