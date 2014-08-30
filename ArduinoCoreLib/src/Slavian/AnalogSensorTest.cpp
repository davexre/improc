#include "Arduino.h"
#include "utils.h"
#include "TicksPerSecond.h"
#include "BlinkingLed.h"
#include "SerialReader.h"
#include "SmoothValue.h"

DefineClass(AnalogSensorTest);

static const int ledPin = 13;			// the number of the LED pin
static const int numberOfSensors = 1;
static const int sensorPins[numberOfSensors] = { 1 };

static const int smoothBufferSize = 20;
static int smoothBuffer[numberOfSensors][smoothBufferSize];

static SmoothValue sval[numberOfSensors];

static TicksPerSecond<> tps;
static BlinkingLed led;
static SerialReader reader;

static char readerBuffer[200];

static unsigned long lastTime;

static float mySmoothVal;
static int smoothedVal = 0;

static DigitalOutputArduinoPin diLedPin;

void AnalogSensorTest::setup() {
	tps.initialize();
	diLedPin.initialize(ledPin);
	led.initialize(&diLedPin);
	led.playBlink(BLINK_FAST, -1);
	reader.initialize(115200, size(readerBuffer), readerBuffer);
	delay(1000);
	for (int i = 0; i < numberOfSensors; i++) {
		int val = analogRead(sensorPins[i]);
		sval[i].initialize(smoothBuffer[i], smoothBufferSize, val);
	}
	mySmoothVal = 0.0;
	lastTime = millis();
}

static void showStatus() {
	Serial.print(tps.getTPS());
	Serial_print("\t");
	for (int i = 0; i < numberOfSensors; i++) {
			Serial_print("\t");
		if (i > 0) {
		}
		Serial.print(sval[i].getAvg());
		Serial_print("\t");
		Serial.print(sval[i].getMin());
		Serial_print("\t");
		Serial.print(sval[i].getMax());
	}
	Serial_print("\t");
	Serial.print(smoothedVal);
	Serial_print("\t");
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

static int smooth(int data, float filterVal, float smoothedVal) {
	if (filterVal > 1) { // check to make sure param's are within range
		filterVal = .99;
	} else if (filterVal <= 0) {
		filterVal = 0;
	}
	smoothedVal = (data * (1 - filterVal)) + (smoothedVal * filterVal);
	return (int) smoothedVal;
}

void AnalogSensorTest::loop() {
	led.update();
	tps.update();
	int val;
	for (int i = 0; i < numberOfSensors; i++) {
		val = analogRead(sensorPins[i]);
		sval[i].addValue(val);
	}
	smoothedVal =  smooth(val, 0.95, smoothedVal);
	tps.smooth(val, &mySmoothVal);

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
