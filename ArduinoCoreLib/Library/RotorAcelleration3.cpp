#include "RotorAcelleration3.h"

void RotorAcelleration3::initialize(uint8_t pinNumberA, uint8_t pinNumberB) {
	pinA.initialize(pinNumberA, 1);
	pinB.initialize(pinNumberB, 1);
	tps.initialize();

	position = 0;
	minValue = 0;
	maxValue = 1000;
}

#define MIN_TPS 5
#define MAX_TPS 20
#define TICKS_AT_MAX_SPEED_FOR_FULL_SPAN 100

void RotorAcelleration3::update() {
	pinA.update(); // toggle
	pinB.update(); // direction

	if (isTicked()) {
		tps.update(true);
		int speed = constrain(tps.getIntTPS_unsafe(), MIN_TPS, MAX_TPS) - MIN_TPS;
		long delta = max(1, (maxValue - minValue) / TICKS_AT_MAX_SPEED_FOR_FULL_SPAN);
		long step = 1 + delta * speed * speed / ((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));
		position = constrain(position + (isIncrementing() ? step : -step),
				minValue, maxValue);
	} else {
		tps.update(false);
	}
}
