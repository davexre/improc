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
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);
	Serial.begin(9600);

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
		Serial.println(btn.isAutoRepeatEnabled() ? "Autorepeat ON" : "Long clicks ON");
	} else if (btn.isClicked()) {
		Serial.println("Click");
	}
}
