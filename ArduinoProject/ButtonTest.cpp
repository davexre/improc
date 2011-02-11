#include "Arduino.h"
#include "Button.h"

DefineClass(ButtonTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static boolean lightOn = false;

void ButtonTest::setup() {
	pinMode(ledPin, OUTPUT);
	btn.initialize(buttonPin);
}

void ButtonTest::loop() {
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
}
