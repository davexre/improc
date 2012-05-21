#include "Arduino.h"
#include "utils.h"
#include "Button.h"

DefineClass(ButtonTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static bool lightOn = false;

static DigitalInputArduinoPin diButtonPin;

void ButtonTest::setup() {
	pinMode(ledPin, OUTPUT);
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);
}

void ButtonTest::loop() {
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
}
