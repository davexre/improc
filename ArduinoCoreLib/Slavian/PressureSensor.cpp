#include "Arduino.h"
#include "utils.h"
#include "TicksPerSecond.h"
#include "BlinkingLed.h"
#include "SerialReader.h"
#include "Button.h"

DefineClass(PressureSensor);

// analog pins
static const int pressurePin = 0;				// pin of the pressre sensor
static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static float pressure = 0.0;

static Button btn;
static bool lightOn = false;
static DigitalInputArduinoPin diButtonPin;

static char buf[20];
static SerialReader reader;
static TicksPerSecond tps;

void PressureSensor::setup() {
	pinMode(ledPin, OUTPUT);
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);

	tps.initialize();
	reader.initialize(115200, size(buf), buf);
}

static int curPressureReading = 0;

void PressureSensor::loop() {
	btn.update();
	tps.update();
	curPressureReading = analogRead(pressurePin);
	tps.smooth(curPressureReading, &pressure, 1);

	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
	if (lightOn) {
		Serial.println((int) curPressureReading);
	} else {
		Serial.println((int) pressure);
	}
}
