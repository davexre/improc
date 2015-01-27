#include "Arduino.h"
#include "utils.h"
#include "SerialReader.h"

const char constStringInProgmem[] PROGMEM = "This string is in PROGMEM";

template<typename dummy=void>
class SerialReaderTest {
	SerialReader reader;
	char buf[200];
public:
	void initialize() {
		reader.initialize(115200, size(buf), buf);
		Serial.println(constStringInProgmem);
		Serial.println("This string is *NOT* in PROGMEM");
		Serial.println(F("This string is ALSO in PROGMEM"));
	}

	void update() {
		reader.update();
		if (reader.available()) {
			char *c = reader.readln();
			Serial.print(F("Got char : "));
			Serial.println(c);
		}
	}
};

DefineClassTemplate(SerialReaderTest)
