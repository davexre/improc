#include "TicksPerSecond.h"

void TicksPerSecond::initialize(const unsigned int holdLastTimeoutMillis) {
	lastTime = millis();
	for (byte i = 0; i < TPS_TIMES_PER_PERIOD; i++) {
		counters[i] = 0;
		started[i] = lastTime;
	}
	curCounter = 0;
	deltaTime = holdLastTimeoutMillis / TPS_TIMES_PER_PERIOD;
}

void TicksPerSecond::update(const boolean tick) {
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
	now -= started[curCounter];
	if (now <= 0)
		now = 1; // This is a division by zero protection.
	tps = (counters[curCounter] * 1000.0) / now;
}

void TicksPerSecond::smooth(const int data, float *smoothedValue, const int timesPerSecond) {
	float scale = getTPS() / timesPerSecond;
	*smoothedValue = (data + (*smoothedValue) * scale) / (scale + 1);
}
