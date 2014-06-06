#include "Arduino.h"
#include "utils.h"
#include "TicksPerSecond.h"
#include "BlinkingLed.h"
#include "SerialReader.h"

DefineClass(GeneratorWithPressureSensor);

// analog pins
static const int pressurePin = 0;				// pin of the pressre sensor
static const int cellTemperaturePin = 1;		// pin of the CELL temperature sensor
static const int mosfetTemperaturePin = 2;		// pin of the MOSFET temperature sensor
static const int ambientTemperaturePin = 3;		// pin of the AMBIENT temperature sensor
static const int currentPin = 4;				// pin of the CURRENT sensor

// digital pins
static const int mosfetPin = 8;
static const int ledPin = 13;					// LED pin

static const float shuntResistance = 0.275;	// Shunt resistance in Ohms
static const float analogReadingToVoltage = 5.0 / 1024.0;

static float pressure = 0.0;
static float cellTemperature = 0.0;
static float mosfetTemperature = 0.0;
static float ambientTemperature = 0.0;
static float current = 0.0;

static int pressureTreshold = 140;
static int cellTemperatureThreshold = (int) (60 / analogReadingToVoltage / 100.0);		// 60 degrees
static int mosfetTemperatureThreshold = (int) (80 / analogReadingToVoltage / 100.0);	// 80 degrees
static int currentMaxThreshold = (int) (10 * shuntResistance / analogReadingToVoltage);	// 10 amps
static int currentMinThreshold = (int) (0.020 * shuntResistance / analogReadingToVoltage);	// 0.020 amps

static bool isPlaying = false;
static int maxPressure = 0;
static int maxCurrent = 0;

static BlinkingLed led;

static char buf[20];

static SerialReader reader;
static TicksPerSecond<> tps;
static TicksPerSecond<> currentTPS;

static void showStatus(bool aborting) {
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

static bool checkThresholdsExceeded() {
	return (
		(pressure >= pressureTreshold) |
		(cellTemperature >= cellTemperatureThreshold) |
		(mosfetTemperature >= mosfetTemperatureThreshold) |
		(current >= currentMaxThreshold) );
}

static long frequency;
static long temp;
static void processReader() {
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

static int tempCurrent;

static DigitalOutputArduinoPin diLedPin;

void GeneratorWithPressureSensor::setup() {
	noTone(mosfetPin);
	pinMode(mosfetPin, OUTPUT);
	tps.initialize();
	currentTPS.initialize();
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin);
	reader.initialize(115200, size(buf), buf);
}

void GeneratorWithPressureSensor::loop() {
	tps.update();
	led.update();

	tps.smooth(analogRead(pressurePin), &pressure, 4);
	tps.smooth(analogRead(cellTemperaturePin), &cellTemperature, 4);
	tps.smooth(analogRead(mosfetTemperaturePin), &mosfetTemperature, 4);
	tps.smooth(analogRead(ambientTemperaturePin), &ambientTemperature, 4);
	tempCurrent = analogRead(currentPin);
	if (tempCurrent > currentMinThreshold) {
		currentTPS.update();
		currentTPS.smooth(tempCurrent, &current, 4);
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
