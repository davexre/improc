#include "RotorAcelleration2.h"

void RotorAcelleration2::initialize(int pinA, int pinB) {
	this->pinA = pinA;
	this->pinB = pinB;
	debounce = 7;
	time0 = 10;
	time1 = 60;
	steps[0] = 100;
	steps[1] = 10;
	steps[2] = 1;
	position = 0;
	minValue = 0;
	maxValue = 1000;
    // Init ports & turn on pullup resistors
    pinMode(pinA, INPUT);
    digitalWrite(pinA, HIGH);
    pinMode(pinB, INPUT);
    digitalWrite(pinB, HIGH);
    lastTimeToggleB = lastTimeToggleA = millis();
    pinAState = digitalRead(pinA);
    pinBState = digitalRead(pinB);
}

void RotorAcelleration2::update() {
	long now = millis();
	long timeExpire = now - time1 << 1;
	boolean toggle = digitalRead(pinA) == HIGH;
	boolean direction = digitalRead(pinB) == HIGH;
	if (direction != pinBState) {
		if (now - lastTimeToggleB > debounce) {
			pinBState = direction;
		}
		lastTimeToggleB = now;
	} else if (lastTimeToggleB < timeExpire) {
		lastTimeToggleB = timeExpire;	// no toggle for too long
	}

	if (toggle != pinAState) {
		if (now - lastTimeToggleA > debounce) {
			pinAState = toggle;
			if (pinAState) {
				long delta = now - lastTimeToggleA;
				int step = delta < time0 ? 0 : (delta < time1 ? 1 : 2);
				position = constrain(position + (direction ? steps[step] : -steps[step]),
						minValue, maxValue);
			}
		}
		lastTimeToggleA = now;
	} else if (lastTimeToggleA < timeExpire) {
		lastTimeToggleA = timeExpire;	// no toggle for too long
	}
}
