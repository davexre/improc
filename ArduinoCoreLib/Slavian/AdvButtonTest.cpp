#include "Arduino.h"
#include "AdvButton.h"
#include "StateLed.h"

DefineClass(AdvButtonTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static StateLed led;
static AdvButton btn;
static bool lightOn = false;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_FAST
};

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;

void AdvButtonTest::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin);
	led.initialize(&diLedPin, states, size(states), true);
	Serial.begin(115200);

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
