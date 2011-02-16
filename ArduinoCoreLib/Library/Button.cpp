#include "Button.h"

void Button::initialize(uint8_t pin, int debounceMillis) {
	buttonPin = pin;
	debounce = debounceMillis;
	pinMode(pin, INPUT);
	digitalWrite(pin, HIGH);
	lastToggleTime = millis();
	lastPortReading = lastState = buttonState = digitalRead(buttonPin);
}

void Button::update() {
	boolean curReading = digitalRead(buttonPin);
	long tmpTime = millis();
	lastState = buttonState;
	if (curReading != lastPortReading) {
		lastToggleTime = tmpTime;
	} else {
		tmpTime -= debounce;
		if (tmpTime >= lastToggleTime) {
			// Button state has not changed for #debounce# millis. Consider it is stable.
			buttonState = curReading;
			// Forward the last toggle time a bit
			lastToggleTime = tmpTime;
		}
	}
	lastPortReading = curReading;
}
