#include "TicksPerSecond.h"

void TicksPerSecond::initialize(const unsigned int holdLastTimeoutMillis) {
	lastTime = millis();
	for (byte i = 0; i < TPS_TIMES_PER_PERIOD; i++) {
		counters[i] = 0;
		started[i] = lastTime;
	}
	dT = 1;
	curCounter = 0;
	deltaTime = holdLastTimeoutMillis / TPS_TIMES_PER_PERIOD;
}

void TicksPerSecond::update(const bool tick) {
	unsigned long now = millis();
	if (now - lastTime >= deltaTime) {
		counters[curCounter] = 0;
		lastTime = started[curCounter++] = now;
		if (curCounter >= TPS_TIMES_PER_PERIOD)
			curCounter = 0;
	}
	if (tick) {
		for (byte i = 0; i < TPS_TIMES_PER_PERIOD; i++) {
			counters[i]++;
		}
	}
	dT = now - started[curCounter];
	if (dT == 0)
		dT = 1; // This is a division by zero protection.
}

float TicksPerSecond::getTPS_unsafe() {
	return ((float)(counters[curCounter]) * 1000.0f) / (float)dT;
}

int TicksPerSecond::getIntTPS_unsafe() {
	return (counters[curCounter] * 1000UL) / dT;
}

void TicksPerSecond::smooth(const int data, float *smoothedValue, const int timesPerSecond) {
	float scale = getTPS() / timesPerSecond;
	*smoothedValue = (data + (*smoothedValue) * scale) / (scale + 1);
}
