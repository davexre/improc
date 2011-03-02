#include "Arduino.h"
#include "AdvButton.h"
#include "StateLed.h"

DefineClass(AdvButtonTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static StateLed led;
static AdvButton btn;
static boolean lightOn = false;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_FAST
};

void AdvButtonTest::setup() {
	pinMode(ledPin, OUTPUT);
	btn.initialize(buttonPin);
	led.initialize(ledPin, size(states), states, true);
	Serial.begin(9600);

	btn.setAutoRepeatEnabled(false);
	led.setState(btn.isAutoRepeatEnabled());
	Serial.println("Double click to toggle [Long clicks]/[Auto repeat clicks]");
}

void AdvButtonTest::loop() {
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		Serial.println("Long click");
	} else if (btn.isDoubleClicked()) {
		Serial.println("Double click");
		btn.setAutoRepeatEnabled(!btn.isAutoRepeatEnabled());
		led.setState(btn.isAutoRepeatEnabled());
	} else if (btn.isClicked()) {
		Serial.println("Click");
	}
}
