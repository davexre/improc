#ifndef TicksPerSecond_h
#define TicksPerSecond_h

#include <wiring.h>
#include <utils.h>

/**
 * Used to compute the looping speed of the main loop
 * TPS - Ticks Per Second.
 * Speed is measured TPS_TIMES_PER_PERIOD times.
 * This value should be atleast 2.
 */
#define TPS_TIMES_PER_PERIOD 3

class TicksPerSecond {
private:
	/**
	 * Tick counters
	 */
	unsigned long counters[TPS_TIMES_PER_PERIOD];

	/**
	 * Tick counter start times
	 */
	unsigned long started[TPS_TIMES_PER_PERIOD];

	/**
	 * Shows the current started/counters pair in use for calculating the
	 * ticks per second value
	 */
	byte curCounter;

	/**
	 * The last time the update() method was invoked
	 */
	unsigned long lastTime;

	unsigned long dT;

	/**
	 * After deltaTime milliseconds the next strated/counters pair will be used
	 */
	unsigned int deltaTime;
public:
	/**
	 * Initializes the class.
	 *
	 * holdLastTimeoutMillis
	 * 		The period for calculating the ticks per second. The default is 500.
	 * 		This means that for 1/2 second all ticks will be counted and the value
	 * 		for ticks per second will be calculated.
	 */
	void initialize(const unsigned int holdLastTimeoutMillis = 500);

	/**
	 * Calculates and updates the ticks per second value.
	 * The update() method should be called as frequent as possible. The method can
	 * be called from an interrupt if applicable. If the method is called from
	 * interrupt the "safe" methods getTPS(), getIntTPS() for getting the ticks per
	 * second value should be used.
	 */
	void update();

	void tick();

	/**
	 * Returns the ticks per second value.
	 * Disables interrupts before reading the actual value.
	 */
	inline float getTPS() {
		disableInterrupts();
		float result = getTPS_unsafe();
		restoreInterrupts();
		return result;
	}

	/**
	 * Returns the ticks per second value.
	 * This is "thread unsafe" method.
	 */
	float getTPS_unsafe();

	/**
	 * Returns the ticks per second value as integer.
	 * Disables interrupts before reading the actual value.
	 */
	inline int getIntTPS() {
		disableInterrupts();
		int result = getIntTPS_unsafe();
		restoreInterrupts();
		return result;
	}

	/**
	 * Returns the ticks per second value as integer.
	 * This is "thread unsafe" method.
	 */
	int getIntTPS_unsafe();

	/**
	 * Smoothes a value.
	 * Can be used for smoothing the results of measuring data from temperature sensors.
	 *
	 * timesPerSecond
	 * 		This is equivalent to making an average for all measurements timesPerSecond.
	 *
	 * Example:
	 *
	 * TicksPerPeriod tps;
	 *
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
	void smooth(const int data, float *smoothedValue, const int timesPerSecond = 1);
};

#endif
