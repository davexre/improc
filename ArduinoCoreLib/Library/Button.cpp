#include "Button.h"

void Button::initialize(DigitalInputPin *pin, const unsigned int debounceMillis) {
	buttonPin = pin;
	debounce = debounceMillis;
	lastToggleTime = millis();
	lastState = currentState = buttonPin->getState();
}

void Button::update() {
	bool curReading = buttonPin->getState();
	unsigned long now = millis();
	lastState = currentState;
	if (curReading != currentState) {
		if (now - lastToggleTime >= debounce) {
			// Button state has not changed for #debounce# milliseconds. Consider it is stable.
			currentState = curReading;
		}
		lastToggleTime = now;
	} else if (now - lastToggleTime >= debounce) {
		// Forward the last toggle time a bit
		lastToggleTime = now - debounce - 1;
	}
}

void Button::reset() {
	lastState = currentState;
	lastToggleTime = millis() - debounce - 1;
}
