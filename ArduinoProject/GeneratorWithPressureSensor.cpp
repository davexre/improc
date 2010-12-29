//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "SerialReader.h"

// analog pins
const int pressurePin = 0;				// pin of the pressre sensor
const int cellTemperaturePin = 1;		// pin of the CELL temperature sensor
const int mosfetTemperaturePin = 2;		// pin of the MOSFET temperature sensor
const int ambientTemperaturePin = 3;	// pin of the AMBIENT temperature sensor
const int currentPin = 4;				// pin of the CURRENT sensor

// digital pins
const int mosfetPin = 8;
const int ledPin = 13;					// LED pin

const float shuntResistance = 0.275;	// Shunt resistance in Ohms
const float analogReadingToVoltage = 5.0 / 1024.0;

float pressure = 0.0;
float cellTemperature = 0.0;
float mosfetTemperature = 0.0;
float ambientTemperature = 0.0;
float current = 0.0;

int pressureTreshold = 140;
int cellTemperatureThreshold = (int) (60 / analogReadingToVoltage / 100.0);		// 60 degrees
int mosfetTemperatureThreshold = (int) (80 / analogReadingToVoltage / 100.0);	// 80 degrees
int currentMaxThreshold = (int) (10 * shuntResistance / analogReadingToVoltage);	// 10 amps
int currentMinThreshold = (int) (0.020 * shuntResistance / analogReadingToVoltage);	// 0.020 amps

boolean isPlaying = false;
int maxPressure = 0;
int maxCurrent = 0;

BlinkingLed led;

char buf[20];

RPS currentRPS;

extern "C" void setup() {
	noTone(mosfetPin);
	pinMode(mosfetPin, OUTPUT);
	rps.initialize();
	currentRPS.initialize();
	led.initialize(ledPin);
	reader.initialize(9600, size(buf), buf);
}

void showStatus(boolean aborting) {
	Serial.print(isPlaying ? "1" : "0");
	Serial.print("\t");
	Serial.print(aborting ? "1" : "0");
	Serial.print("\t");
	Serial.print((int) pressure);
	Serial.print("\t");
	Serial.print((int) cellTemperature);
	Serial.print("\t");
	Serial.print((int) mosfetTemperature);
	Serial.print("\t");
	Serial.print((int) ambientTemperature);
	Serial.print("\t");
	Serial.print((int) current);
	Serial.print("\t");
	Serial.print(maxPressure);
	Serial.print("\t");
	Serial.print(maxCurrent);
	Serial.println();
}

boolean checkThresholdsExceeded() {
	return (
		(pressure >= pressureTreshold) |
		(cellTemperature >= cellTemperatureThreshold) |
		(mosfetTemperature >= mosfetTemperatureThreshold) |
		(current >= currentMaxThreshold) );
}

long frequency;
long temp;
void processReader() {
	char *c;
	int lightOn;
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	switch (c++[0]) {
	case 'a':	// Set cell temperature threshold
		cellTemperatureThreshold = strtol(c, &c, 10);
		break;
	case 'b':	// Set mosfet temperature threshold
		mosfetTemperatureThreshold = strtol(c, &c, 10);
		break;
	case 'd':	// Set current threshold
		currentMaxThreshold = strtol(c, &c, 10);
		break;
	case 'f':	// fix to always on/off
		temp = strtol(c, &c, 10);
		frequency = 0;
		pinMode(mosfetPin, OUTPUT);
		if ((temp == 0) || checkThresholdsExceeded()) {
			digitalWrite(mosfetPin, LOW);
			isPlaying = false;
		} else {
			digitalWrite(mosfetPin, HIGH);
			isPlaying = true;
		}
		led.playBlink(BLINK_FAST, isPlaying ? -1 : 0);
		break;
	case 't':	// Set pressure threshold
		pressureTreshold = strtol(c, &c, 10);
		break;
	case 's':	// Set new frequency. If frequency == 0 turn off
		frequency = strtol(c, &c, 10);
		maxPressure = pressure;
		maxCurrent = current;
		if ((frequency == 0) || checkThresholdsExceeded()) {
			isPlaying = false;
			noTone(mosfetPin);
		} else {
			isPlaying = true;
			tone(mosfetPin, frequency);
		}
		led.playBlink(BLINK_FAST, isPlaying ? -1 : 0);
		break;
	case 'l':
		showStatus(false);
		break;
	case 'z':
		Serial.print("cellTemperatureThreshold=");
		Serial.println(cellTemperatureThreshold);
		Serial.print("mosfetTemperatureThreshold=");
		Serial.println(mosfetTemperatureThreshold);
		Serial.print("currentMaxThreshold=");
		Serial.println(currentMaxThreshold);
		Serial.print("pressureTreshold=");
		Serial.println(pressureTreshold);
		break;
	}
}

int tempCurrent;
extern "C" void loop() {
	rps.update();
	led.update();

	rps.smooth(analogRead(pressurePin), &pressure, 4);
	rps.smooth(analogRead(cellTemperaturePin), &cellTemperature, 4);
	rps.smooth(analogRead(mosfetTemperaturePin), &mosfetTemperature, 4);
	rps.smooth(analogRead(ambientTemperaturePin), &ambientTemperature, 4);
	tempCurrent = analogRead(currentPin);
	if (tempCurrent > currentMinThreshold) {
		currentRPS.update();
		currentRPS.smooth(tempCurrent, &current, 4);
	}

	processReader();
	if (maxPressure < pressure)
		maxPressure = pressure;
	if (maxCurrent < current)
		maxCurrent = current;
	if (checkThresholdsExceeded()) {
		// turn off
		noTone(mosfetPin);
		led.playBlink(BLINK_FAST, 0);
		if (isPlaying) {
			isPlaying = false;
			showStatus(true);
		}
	}
}

#endif
