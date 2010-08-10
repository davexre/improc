#include "Button.h"

void Button::initialize(int pin) {
	buttonPin = pin;
	debounce = 50;
	pinMode(pin, INPUT);
	digitalWrite(pin, HIGH);
	lastTime = millis();
	lastState = buttonState = digitalRead(buttonPin);
}

void Button::update() {
	long now = millis();
	if (now - lastTime > debounce) {
		lastState = buttonState;
		buttonState = digitalRead(buttonPin);
		lastTime = now;
	}
}
