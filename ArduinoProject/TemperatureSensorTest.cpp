#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "SerialReader.h"
#include "SmoothValue.h"

const int ledPin = 13;			// the number of the LED pin
const int numberOfTemperaturePins = 3;
const int temperaturePins[numberOfTemperaturePins] = { 1, 2, 3 };

const int temperatureBufSize = 10;
int temperatureBuf[numberOfTemperaturePins][temperatureBufSize];

SmoothValue sval[numberOfTemperaturePins];

BlinkingLed led;

char buf[200];

long lastTime;

extern "C" void setup() {
	led.initialize(ledPin);
	led.playBlink(BLINK_FAST, -1);
	reader.initialize(9600, size(buf), buf);
	for (int i = 0; i < numberOfTemperaturePins; i++) {
		int val = analogRead(temperaturePins[i]);
		sval[i].initialize(temperatureBuf[i], temperatureBufSize, val);
	}
	lastTime = millis();
}

void showStatus() {
	for (int i = 0; i < numberOfTemperaturePins; i++) {
		if (i > 0) {
			Serial.print("\t");
		}
		Serial.print(sval[i].getAvg());
	}
	Serial.println();
}
/*
void processReader() {
	char *c;
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	switch (c++[0]) {
	case 'l':	// list
		showStatus();
		break;
	case 'c':	// clear
		memset(curReading, 0, sizeof(curReading));
		break;
	}
}
*/

extern "C" void loop() {
	led.update();
	for (int i = 0; i < numberOfTemperaturePins; i++) {
		int val = analogRead(temperaturePins[i]);
		sval[i].addValue(val);
	}
//	processReader();
	if (millis() - lastTime > 100) {
		showStatus();
		lastTime = millis();
		for (int i = 0; i < numberOfTemperaturePins; i++) {
			int val = analogRead(temperaturePins[i]);
			sval[i].reset(val);
		}
	}
}

#endif
