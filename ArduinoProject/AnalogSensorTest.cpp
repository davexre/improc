//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "SerialReader.h"
#include "SmoothValue.h"

const int ledPin = 13;			// the number of the LED pin
const int numberOfSensors = 1;
const int sensorPins[numberOfSensors] = { 1 };

const int smoothBufferSize = 20;
int smoothBuffer[numberOfSensors][smoothBufferSize];

SmoothValue sval[numberOfSensors];

RPS rps;
BlinkingLed led;
SerialReader reader;

char readerBuffer[200];

long lastTime;

float mySmoothVal;

extern "C" void setup() {
	rps.initialize();
	led.initialize(ledPin);
	led.playBlink(BLINK_FAST, -1);
	reader.initialize(9600, size(readerBuffer), readerBuffer);
	delay(1000);
	for (int i = 0; i < numberOfSensors; i++) {
		int val = analogRead(sensorPins[i]);
		sval[i].initialize(smoothBuffer[i], smoothBufferSize, val);
	}
	mySmoothVal = 0.0;
	lastTime = millis();
}

int smoothedVal = 0;

void showStatus() {
	Serial.print(rps.rps);
	Serial.print("\t");
	for (int i = 0; i < numberOfSensors; i++) {
			Serial.print("\t");
		if (i > 0) {
		}
		Serial.print(sval[i].getAvg());
		Serial.print("\t");
		Serial.print(sval[i].getMin());
		Serial.print("\t");
		Serial.print(sval[i].getMax());
	}
	Serial.print("\t");
	Serial.print(smoothedVal);
	Serial.print("\t");
	Serial.print((int) mySmoothVal);
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

int smooth(int data, float filterVal, float smoothedVal) {
	if (filterVal > 1) { // check to make sure param's are within range
		filterVal = .99;
	} else if (filterVal <= 0) {
		filterVal = 0;
	}
	smoothedVal = (data * (1 - filterVal)) + (smoothedVal * filterVal);
	return (int) smoothedVal;
}

extern "C" void loop() {
	led.update();
	rps.update();
	int val;
	for (int i = 0; i < numberOfSensors; i++) {
		val = analogRead(sensorPins[i]);
		sval[i].addValue(val);
	}
	smoothedVal =  smooth(val, 0.95, smoothedVal);
	rps.smooth(val, &mySmoothVal);

//	processReader();
	if (millis() - lastTime > 500) {
		showStatus();
		lastTime = millis();
		for (int i = 0; i < numberOfSensors; i++) {
			val = analogRead(sensorPins[i]);
			sval[i].reset(val);
			smoothedVal = val;
		}
	}
}

#endif
