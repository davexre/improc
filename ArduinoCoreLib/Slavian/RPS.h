#ifndef RPS_h
#define RPS_h

#include <wiring.h>

/**
 * Used to compute the looping speed of the main loop
 * RPS - Rotations Per Second.
 * Speed is measured RPS_TIMES_PER_SECOND times in a second.
 */
#define RPS_TIMES_PER_SECOND 5

class RPS {
private:
	long counters[RPS_TIMES_PER_SECOND];
	long started[RPS_TIMES_PER_SECOND];
	long lastTime;
	int deltaTime;
	byte curCounter;
public:
	long rps;
	void initialize();
	void update(boolean tick = true);
	/**
	 * Smothes a value.
	 * timesPerSecond - equvalent to making an average for all measurements timesPerSecond.
	 * Each call to smooth on a speciffic smoothedValue should be preceeded by a call to rps.update().
	 * ex:
	 * void setup() {
	 *   rps.initialize();
	 * }
	 *
	 * float val1;
	 * float val2;
	 *
	 * void loop() {
	 *   rps.update();
	 *   rps.smooth(analogRead(1), val1);
	 *   rps.smooth(analogRead(2), val2);
	 * }
	 */
	void smooth(int data, float *smoothedValue, int timesPerSecond = 4);
};

#endif
