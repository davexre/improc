#include "Arduino.h"
#include "utils.h"
#include "SerialReader.h"

template<typename dummy=void>
class SerialReaderTest {
	SerialReader reader;
	char buf[200];
public:
	void initialize() {
		reader.initialize(115200, size(buf), buf);
	}

	void update() {
		reader.update();
		if (reader.available()) {
			char *c = reader.readln();
			Serial.println(c);
		}
	}
};

