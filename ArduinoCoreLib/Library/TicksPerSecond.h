#ifndef TicksPerSecond_h
#define TicksPerSecond_h

#include "Arduino.h"
#include <wiring.h>

/**
 * Used to compute the looping speed of the main loop
 * TPS - Ticks Per Second.
 * Speed is measured TPS_TIMES_PER_SECOND times in a second.
 */
#define TPS_TIMES_PER_SECOND 5

class TicksPerSecond {
private:
	long counters[TPS_TIMES_PER_SECOND];
	long started[TPS_TIMES_PER_SECOND];
	long lastTime;
	int deltaTime;
	byte curCounter;
	volatile float tps;
public:
	void initialize(int holdLastTimeoutMillis = 500);
	void update(boolean tick = true);

	inline float getTPS() {
		disableInterrupts();
		float result = tps;
		restoreInterrupts();
		return result;
	}

	inline float getTPS_unsafe() {
		return tps;
	}

	inline int getIntTPS() {
		disableInterrupts();
		int result = (int) tps;
		restoreInterrupts();
		return result;
	}

	inline int getIntTPS_unsafe() {
		return (int) tps;
	}

	/**
	 * Smothes a value.
	 * timesPerSecond - equvalent to making an average for all measurements timesPerSecond.
	 * Each call to smooth on a speciffic smoothedValue should be preceeded by a call to tps.update().
	 * ex:
	 * void setup() {
	 *   tps.initialize();
	 * }
	 *
	 * float val1;
	 * float val2;
	 *
	 * void loop() {
	 *   tps.update();
	 *   tps.smooth(analogRead(1), val1);
	 *   tps.smooth(analogRead(2), val2);
	 * }
	 */
	void smooth(int data, float *smoothedValue, int timesPerSecond = 4);
};

#endif
