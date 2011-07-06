#include "Arduino.h"
#include "Button.h"
#include "TicksPerSecond.h"

DefineClass(TicksPerSecondTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static bool lightOn = false;
static TicksPerSecond tps;

unsigned long start, count;

void TicksPerSecondTest::setup() {
	pinMode(ledPin, OUTPUT);
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
	tps.initialize();
	Serial.begin(115200);
	start = millis();
	count = 0;
}

void TicksPerSecondTest::loop() {
/*
	btn.update();
	tps.update(true);
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
		Serial.println(tps.getTPS());
	}
 */
/*	btn.update();
	if (btn.isPressed()) {
		tps.update(true);
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	} else {
		tps.update(false);
	}
	Serial.println(tps.getTPS()); // Print button presses presses per second
*/
	tps.update(true);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	tps.update(false);
	count++;
	unsigned long dt = millis() - start;
	if (dt >= 5000) {
		float f = (float)count * 1000.0 / (float)dt;
		Serial.println(f);
		Serial.println(tps.getTPS());
		Serial.println(tps.getIntTPS());
		Serial.println("--");
		count = 0;
		start = millis();
	}
}
