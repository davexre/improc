#include "RotaryEncoderAcelleration.h"

void RotaryEncoderAcelleration::initialize(DigitalInputArduinoPin *pinA, DigitalInputArduinoPin *pinB) {
	initialState.initialize(0, 1000, false);
	state = &initialState;
	this->pinA.initialize(pinA, 1);
	this->pinB.initialize(pinB, 1);
	tps.initialize();
}

void RotaryEncoderAcelleration::update() {
	pinA.update(); // toggle
	pinB.update(); // direction

	if (state->valueChangeEnabled && isTicked()) {
		tps.update(true);
		int speed = constrain(tps.getIntTPS_unsafe(), MIN_TPS, MAX_TPS) - MIN_TPS;
		long delta = max(1, (state->maxValue - state->minValue) / TICKS_AT_MAX_SPEED_FOR_FULL_SPAN);

		// Linear acceleration (very sensitive - not comfortable)
		// long step = 1 + delta * speed / (MAX_TPS - MIN_TPS);

		// Exponential acceleration - square (OK for [maxValue - minValue] = up to 5000)
		// long step = 1 + delta * speed * speed / ((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

		// Exponential acceleration - cubic (most comfortable)
		long step = 1 + delta * speed * speed * speed /
				((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

		state->setValue_unsafe(state->getValue_unsafe() + (isIncrementing() ? step : -step));
	} else {
		tps.update(false);
	}
}

void RotaryEncoderState::initialize(const long minVal, const long maxVal, const boolean looped) {
	_isValueLooped = looped;
	_hasValueChanged = true;
	valueChangeEnabled = true;
	value = minVal;
	minValue = minVal;
	maxValue = maxVal;
}

void RotaryEncoderState::setValue_unsafe(long newValue) {
	if (_isValueLooped) {
		long delta = maxValue - minValue + 1;
		if (delta <= 0)
			delta = 1;
		while (newValue > maxValue)
			newValue -= delta;
		while (newValue < minValue)
			newValue += delta;
		value = newValue;
	} else {
		value = constrain(newValue, minValue, maxValue);
	}
	_hasValueChanged = true;
}

void RotaryEncoderState::setMinMax(const long newMinValue, const long newMaxValue) {
	disableInterrupts();
	minValue = newMinValue;
	maxValue = newMaxValue;
	setValue_unsafe(value);
	restoreInterrupts();
}
