#ifndef BLINKINGLED_H_
#define BLINKINGLED_H_

#include <WProgram.h>

/**
 * Blinks a led.
 * The led on/off is specified as time delays (in milliseconds)
 * Uses the timer0 via the millis() function.
 */
class BlinkingLed {
private:
public:
	unsigned int getNextDelay(void);
	uint8_t curDelay;
	long toggleTime;
	const unsigned int *delays;

	uint8_t pin;
	boolean lightOn;
	int playCount;
	void stop();
	void play(int playCount);
	void initialilze(uint8_t pin);
	void playBlink(const unsigned int *delays, int playCount);
	void update(void);
	inline boolean isPlaying() {
		return ((playCount != 0) && (delays != NULL));
	};
};

// Blinking is defined as sequence of led on/off times (in millis)
// LedOn, LedOff, LedOn, LedOff, 0
const unsigned int BLINK_LONG_DELAY = 500;
const unsigned int BLINK_LONG_MEDIUM = 250;
const unsigned int BLINK_LONG_SHORT = 50;

const unsigned int BLINK_SLOW[] = {
		BLINK_LONG_DELAY, BLINK_LONG_DELAY,
		0};
const unsigned int BLINK_MEDIUM[] = {
		BLINK_LONG_MEDIUM, BLINK_LONG_MEDIUM,
		0};
const unsigned int BLINK_FAST[] = {
		BLINK_LONG_SHORT, BLINK_LONG_SHORT,
		0};
const unsigned int BLINK_OFF[] = {0};
const unsigned int BLINK_ON[] = {BLINK_LONG_SHORT, 0};

const unsigned int BLINK1[] = {
		BLINK_LONG_SHORT, BLINK_LONG_SHORT,
		BLINK_LONG_MEDIUM, BLINK_LONG_MEDIUM,
		0};
const unsigned int BLINK2[] = {
		BLINK_LONG_SHORT, BLINK_LONG_MEDIUM,
		BLINK_LONG_SHORT, BLINK_LONG_MEDIUM,
		BLINK_LONG_MEDIUM, BLINK_LONG_MEDIUM,
		0};
const unsigned int BLINK3[] = {
		BLINK_LONG_SHORT, BLINK_LONG_DELAY,
		0};

#endif
