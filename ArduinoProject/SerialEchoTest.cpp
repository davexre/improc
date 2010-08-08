#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"

const int ledPin = 13;		// the number of the LED pin

class SerialReader {
private:
	int bufferSize;
	int bufferFull;
	char *buffer;
	boolean eol;
public:
	void initialize(int speed, int bufferSize, char *buffer);
	void setBuffer(int bufferSize, char *buffer);
	void update(void);
	boolean available();
	char *readln();
};

void SerialReader::initialize(int speed, int bufferSize, char *buffer) {
	Serial.begin(speed);
	setBuffer(bufferSize, buffer);
}

void SerialReader::setBuffer(int bufferSize, char *buffer) {
	this->bufferFull = 0;
	this->bufferSize = bufferSize;
	this->buffer = buffer;
	this->buffer[0] = 0;
	eol = false;
}

void SerialReader::update(void) {
	if (eol)
		return;
	while ((bufferSize > bufferFull + 1) && (Serial.available() > 0)) {
		if ((buffer[bufferFull] = Serial.read()) == '\n') {
			eol = true;
			buffer[bufferFull] = 0;
			break;
		}
		buffer[++bufferFull] = 0;
	}
	if (bufferSize <= bufferFull + 1) {
		eol = true;
	}
}

boolean SerialReader::available(void) {
	return eol;
}

char *SerialReader::readln() {
	if (eol) {
		eol = false;
		bufferFull = 0;
		return buffer;
	}
	return NULL;
}

SerialReader reader;
BlinkingLed led;
char buf[200];

extern "C" void setup() {
	led.initialilze(ledPin);
	reader.initialize(9600, size(buf), buf);
}

char *c;
extern "C" void loop() {
	led.update();
	reader.update();
	if (reader.available()) {
		led.playBlink(BLINK_FAST, 3);

		c = reader.readln();
		Serial.print("NUM : ");
		Serial.println(myatoi(&c));
		Serial.print("REST: ");
		Serial.println(c);
	}
}

#endif
