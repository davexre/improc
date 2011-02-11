#include "RPS.h"

void RPS::initialize() {
	lastTime = millis();
	for (byte i = 0; i < RPS_TIMES_PER_SECOND; i++) {
		counters[i] = 0;
		started[i] = lastTime;
	}
	curCounter = 0;
	deltaTime = 1000 / RPS_TIMES_PER_SECOND;
}

void RPS::update(boolean tick) {
	long now = millis();
	if (tick) {
		for (byte i = 0; i < RPS_TIMES_PER_SECOND; i++) {
			counters[i]++;
		}
	}
	if (now - lastTime >= deltaTime) {
		rps = (counters[curCounter] * 1000) / (now - started[curCounter]);
		lastTime = now;
		started[curCounter] = now;
		counters[curCounter++] = 0;
		if (curCounter >= RPS_TIMES_PER_SECOND) {
			curCounter = 0;
		}
	}
}

void RPS::smooth(int data, float *smoothedValue, int timesPerSecond) {
	long scale = rps / timesPerSecond;
	*smoothedValue = (data + (*smoothedValue) * scale) / (scale + 1);
}
