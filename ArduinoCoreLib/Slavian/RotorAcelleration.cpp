#include "RotorAcelleration.h"

RotorAcelleration rotor;

void RotorAcelleration::UpdateRotation() {
	int direction = digitalRead(rotor.pinB);
	long now = millis();
	long delta = now - rotor.lastTime;
	if (delta < rotor.timeDebounce) {
		// debounce pinA
		return;
	}
	int step = delta < rotor.time0 ? 0 : (delta < rotor.time1 ? 1 : 2);
	int curDirection = (direction == HIGH ? 2 : 1);
	if ((step == 0) && (rotor.lastDirection != curDirection)) {
		// debounce pinB
		return;
	}
	rotor.lastDirection = curDirection;
	rotor.lastTime = now;
	rotor.position = constrain(rotor.position +
			(direction ? rotor.steps[step] : -rotor.steps[step]),
			rotor.minValue, rotor.maxValue);
}

void RotorAcelleration::initialize(int pinA, int pinB) {
	this->pinA = pinA;
	this->pinB = pinB;
	timeDebounce = 7;
	time0 = 10;
	time1 = 60;
	steps[0] = 100;
	steps[1] = 10;
	steps[2] = 1;
	position = 0;
	minValue = 0;
	maxValue = 1000;
	lastDirection = 0;
    // Init ports & turn on pullup resistors
    pinMode(pinA, INPUT);
    digitalWrite(pinA, HIGH);
    pinMode(pinB, INPUT);
    digitalWrite(pinB, HIGH);
    lastTime = millis();
    attachInterrupt(0, UpdateRotation, FALLING);
}

void RotorAcelleration::update() {
	long now = millis();
	if (lastTime + time1 + time1 < now) {
		uint8_t oldSREG = SREG;
		cli();
		lastTime += time1; // no rotation for too long
		SREG = oldSREG;
	}
}
