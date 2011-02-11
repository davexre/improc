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
	void play(int playCount);
	void initialize(uint8_t pin);
	void playBlink(const unsigned int *delays, int playCount);
	void update(void);

	inline void start() {
		play(-1);
	}

	inline void stop() {
		play(0);
	}

	inline boolean isPlaying() {
		return (playCount); // (playCount != 0)
	};
};

// Blinking is defined as sequence of led on/off times (in millis)
// LedOn, LedOff, LedOn, LedOff, 0
const unsigned int BLINK_DELAY_LONG = 500;
const unsigned int BLINK_DELAY_MEDIUM = 250;
const unsigned int BLINK_DELAY_SHORT = 50;

const unsigned int BLINK_SLOW[] = {
		BLINK_DELAY_LONG, BLINK_DELAY_LONG,
		0};
const unsigned int BLINK_MEDIUM[] = {
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int BLINK_FAST[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		0};
const unsigned int BLINK_OFF[] = {0};
const unsigned int BLINK_ON[] = {BLINK_DELAY_SHORT, 0};

const unsigned int BLINK1[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int BLINK2[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int BLINK3[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_LONG,
		0};

#endif
