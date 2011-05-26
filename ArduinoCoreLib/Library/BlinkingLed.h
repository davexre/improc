#ifndef BLINKINGLED_H_
#define BLINKINGLED_H_

#include "DigitalIO.h"
#include <stddef.h>
#include <wiring.h>
#include <avr/interrupt.h>

/**
 * Blinks a led.
 * The led on/off is specified as time delays (in milliseconds)
 * Uses the timer0 via the millis() function.
 */
class BlinkingLed {
private:
	DigitalOutputPin *pin;

	const unsigned int *delays;
	uint8_t curDelay;

	unsigned long toggleTime;
	signed short int playCount;
	boolean lightOn;

	unsigned int getNextDelay(void);
public:
	/**
	 * Initializes the class.
	 *
	 * pin	The pin number the led is attached to.
	 */
	void initialize(DigitalOutputPin *pin);

	/**
	 * Updates the on/off state of the led. This method should be
	 * placed in the main loop of the program or might be invoked
	 * from an interrupt.
	 */
	void update(void);

	/**
	 * Starts playing the current blink sequence of led on/offs.
	 * This method is "thread safe"
	 *
	 * playCount
	 * 			The number of play backs to be performed on the
	 * 			current blink sequence. A negative value -1 means
	 * 			loop forever.
	 */
	void play(const signed short int playCount);

	/**
	 * Starts playing the current blink sequence of led on/offs.
	 * This method is "thread safe"
	 *
	 * delays
	 * 			Pointer to the blink sequence of led on/offs.
	 * 			A blink sequence must be terminated by a 0 value.
	 * playCount
	 * 			The number of play backs to be performed on the
	 * 			current blink sequence. A negative value -1 means
	 * 			loop forever.
	 */
	void playBlink(const unsigned int *delays, signed short int playCount);

	/**
	 * Starts playing the current blink sequence.
	 * This method is "thread safe"
	 */
	inline void start() {
		play(-1);
	}

	/**
	 * Stops playing the current blink sequence.
	 * This method is "thread safe"
	 */
	inline void stop() {
		play(0);
	}

	/**
	 * Returns TRUE if blink sequence is currently playing.
	 * This method is "thread safe"
	 */
	inline boolean isPlaying() {
		return (playCount); // (playCount != 0)
	};

	/**
	 * Returns the state of the led.
	 * This method is "thread safe"
	 */
	inline boolean isLedOn() {
		return (lightOn);
	}

	/**
	 * Returns the remaining number of loops for the current
	 * blink sequence. If the value is negative the blink
	 * sequence is looped.
	 * This method is "thread safe"
	 */
	inline signed short int getPlayCount() {
		return (playCount);
	}
};

// Blinking is defined as sequence of led on/off times (in milliseconds)
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
