#include "SoftwarePWM.h"

#define MaxPeriodMillis (60UL * 1000UL)

void SoftwarePWM::initialize(DigitalOutputPin *pin, unsigned int frequencyCyclesPerMinute) {
	this->pin = pin;
	pin->setState(0);
	this->frequencyCyclesPerMinute = frequencyCyclesPerMinute;
	this->value = 0;
	this->toggleTime = millis() - MaxPeriodMillis;
}

void SoftwarePWM::update() {
	boolean state = pin->getState();
	unsigned long now = millis();
	if ((value == 0) || (frequencyCyclesPerMinute == 0)) {
		toggleTime = now - MaxPeriodMillis;
		if (state)
			pin->setState(false);
	} else if (value == 255) {
		toggleTime = now - MaxPeriodMillis;
		if (!state)
			pin->setState(true);
	} else {
		unsigned long toggleDelayMillis = ((unsigned long)(state ? value : 255 - value) * 60 * 1000) /
				((unsigned long) frequencyCyclesPerMinute * 255);
		if (now - toggleTime >= toggleDelayMillis) {
			pin->setState(!state);
			toggleTime = now;
		}
	}
}
