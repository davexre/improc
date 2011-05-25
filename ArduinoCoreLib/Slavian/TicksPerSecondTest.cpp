#include "Arduino.h"
#include "Button.h"
#include "TicksPerSecond.h"

DefineClass(TicksPerSecondTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static boolean lightOn = false;
static TicksPerSecond tps;

void TicksPerSecondTest::setup() {
	pinMode(ledPin, OUTPUT);
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
	tps.initialize();
	Serial.begin(115200);
}

void TicksPerSecondTest::loop() {
	btn.update();
	if (btn.isPressed()) {
		tps.update(true);
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	} else {
		tps.update(false);
	}
	Serial.println(tps.getTPS()); // Print button presses presses per second
}
