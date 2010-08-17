#include "Button.h"

void Button::initialize(int pin) {
	buttonPin = pin;
	debounce = 10;
	pinMode(pin, INPUT);
	digitalWrite(pin, HIGH);
	lastTime = millis();
	lastState = buttonState = digitalRead(buttonPin);
}

void Button::update() {
	long now = millis();
	lastState = buttonState;
	if (now - lastTime > debounce) {
		buttonState = digitalRead(buttonPin);
		lastTime = now;
	}
}
