#include "TicksPerSecond.h"

void TicksPerSecond::initialize(int holdLastTimeoutMillis) {
	lastTime = millis();
	for (byte i = 0; i < TPS_TIMES_PER_SECOND; i++) {
		counters[i] = 0;
		started[i] = lastTime;
	}
	curCounter = 0;
	deltaTime = holdLastTimeoutMillis / TPS_TIMES_PER_SECOND;
}

void TicksPerSecond::update(boolean tick) {
	long now = millis();
	if (now - lastTime >= deltaTime) {
		counters[curCounter] = 0;
		lastTime = started[curCounter++] = now;
		if (curCounter >= TPS_TIMES_PER_SECOND)
			curCounter = 0;
	}
	if (tick) {
		for (byte i = 0; i < TPS_TIMES_PER_SECOND; i++) {
			counters[i]++;
		}
	}
	now -= started[curCounter];
	if (now <= 0)
		now = 1;
	tps = (counters[curCounter] * 1000.0) / now;
}

void TicksPerSecond::smooth(int data, float *smoothedValue, int timesPerSecond) {
	float scale = getTPS() / timesPerSecond;
	*smoothedValue = (data + (*smoothedValue) * scale) / (scale + 1);
}
