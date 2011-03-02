#include "RotaryEncoderAcelleration.h"

void RotaryEncoderAcelleration::initialize(uint8_t pinNumberA, uint8_t pinNumberB) {
	pinA.initialize(pinNumberA, 1);
	pinB.initialize(pinNumberB, 1);
	tps.initialize();

	isValueLooped = false;
	valueChangeEnabled = true;
	position = 0;
	minValue = 0;
	maxValue = 1000;
}

void RotaryEncoderAcelleration::update() {
	pinA.update(); // toggle
	pinB.update(); // direction

	if (valueChangeEnabled && isTicked()) {
		tps.update(true);
		int speed = constrain(tps.getIntTPS_unsafe(), MIN_TPS, MAX_TPS) - MIN_TPS;
		long delta = max(1, (maxValue - minValue) / TICKS_AT_MAX_SPEED_FOR_FULL_SPAN);

		// Linear acceleration (very sensitive - not comfortable)
		// long step = 1 + delta * speed / (MAX_TPS - MIN_TPS);

		// Exponential acceleration - square (OK for [maxValue - minValue] = up to 5000)
		// long step = 1 + delta * speed * speed / ((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

		// Exponential acceleration - cubic (most comfortable)
		long step = 1 + delta * speed * speed * speed /
				((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

		setPosition_unsafe(position + (isIncrementing() ? step : -step));
	} else {
		tps.update(false);
	}
}

void RotaryEncoderAcelleration::setPosition_unsafe(long newPosition) {
	if (isValueLooped) {
		long delta = maxValue - minValue;
		if (delta == 0)
			delta = 1;
		while (position > maxValue)
			position -= delta;
		while (position < minValue)
			position += delta;
	} else {
		position = constrain(newPosition, minValue, maxValue);
	}
}

void RotaryEncoderAcelleration::setMinMax(long newMinValue, long newMaxValue) {
	disableInterrupts();
	minValue = newMinValue;
	maxValue = newMaxValue;
	setPosition_unsafe(position);
	restoreInterrupts();
}
