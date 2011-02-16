#include "RotorAcelleration3.h"

void RotorAcelleration3::initialize(uint8_t pinNumberA, uint8_t pinNumberB) {
	pinA.initialize(pinNumberA, 1);
	pinB.initialize(pinNumberB, 1);

	time0 = 10;
	time1 = 60;
	steps[0] = 100;
	steps[1] = 10;
	steps[2] = 1;
	position = 0;
	minValue = 0;
	maxValue = 1000;
	lastTimeToggleA = millis();
}

void RotorAcelleration3::update() {
	pinA.update(); // toggle
	pinB.update(); // direction

	long now = millis();
	if (isTicked()) {
		long delta = now - lastTimeToggleA;
		lastTimeToggleA = now;
		int step = delta < time0 ? 0 : (delta < time1 ? 1 : 2);
		position = constrain(position + (isIncrementing() ? steps[step] : -steps[step]),
				minValue, maxValue);

	}
}
