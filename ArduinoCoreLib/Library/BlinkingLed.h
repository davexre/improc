#ifndef BLINKINGLED_H_
#define BLINKINGLED_H_

#include "DigitalIO.h"
#include <stddef.h>
#include <avr/interrupt.h>

/**
 * Blinks a led.
 * The led on/off is specified as time delays (in milliseconds)
 * Uses the timer0 via the millis() function.
 */
class BlinkingLed {
private:
	DigitalOutputPin *pin;

	const unsigned int *delays; // Delays must be stored in PROGMEM
	uint8_t curDelay;

	unsigned long toggleTime;
	signed short int playCount;
	bool lightOn;

	unsigned int getNextDelay(void) {
		unsigned int result;
		uint8_t oldSREG = SREG;
		cli();
		if (isPlaying()) {
			result = pgm_read_word(&(delays[curDelay++]));
			if (result == 0) {
				lightOn = false;
				curDelay = 0;
				if (playCount > 0)
					playCount--;
				if (playCount != 0) {
					result = pgm_read_word(&(delays[curDelay++]));
					if (result == 0) {
						curDelay = 0;
						playCount = 0;
					}
				}
			}
		} else {
			result = 0;
		}
		SREG = oldSREG;
		return result;
	}
public:
	/**
	 * Initializes the class.
	 *
	 * pin	The pin number the led is attached to.
	 */
	void initialize(DigitalOutputPin *pin) {
		this->pin = pin;
		this->delays = NULL;
		curDelay = 0;
		lightOn = false;
		playCount = 0;
		toggleTime = 0;
	}

	/**
	 * Updates the on/off state of the led. This method should be
	 * placed in the main loop of the program or might be invoked
	 * from an interrupt.
	 */
	void update(void) {
		if (isPlaying()) {
			unsigned long now = millis();
			if ((signed long) (now - toggleTime) >= 0) {
				unsigned int delta = getNextDelay();
				if (delta == 0) {
					lightOn = false;
				} else {
					lightOn = !lightOn;
				}
				toggleTime = now + delta;
				pin->setState(lightOn);
			}
		} else {
			lightOn = false;
			pin->setState(false);
		}
	}

	/**
	 * Starts playing the current blink sequence of led on/offs.
	 * This method is "thread safe"
	 *
	 * playCount
	 * 			The number of play backs to be performed on the
	 * 			current blink sequence. A negative value -1 means
	 * 			loop forever.
	 */
	void play(const signed short int playCount) {
		if (delays != NULL) {
			if (!isPlaying()) {
				toggleTime = millis();
			}
			this->playCount = playCount;
			update();
		}
	}

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
	void playBlink(const unsigned int *delays, signed short int playCount) {
		if (delays == NULL)
			playCount = 0;
		if (delays == this->delays) {
			this->playCount = playCount;
		} else {
			disableInterrupts();
			curDelay = 0;
			this->delays = delays;
			this->playCount = playCount;
			lightOn = false;
			restoreInterrupts();
			toggleTime = millis();
		}
		update();
	}

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
	inline bool isPlaying() {
		return (playCount); // (playCount != 0)
	};

	/**
	 * Returns the state of the led.
	 * This method is "thread safe"
	 */
	inline bool isLedOn() {
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

static constexpr unsigned int BLINK_SLOW[] PROGMEM = {
		BLINK_DELAY_LONG, BLINK_DELAY_LONG,
		0};
static constexpr unsigned int BLINK_MEDIUM[] PROGMEM = {
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
static constexpr unsigned int BLINK_FAST[] PROGMEM = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		0};
static constexpr unsigned int BLINK_OFF[] PROGMEM = {0};
static constexpr unsigned int BLINK_ON[] PROGMEM = {BLINK_DELAY_SHORT, 0};

static constexpr unsigned int BLINK1[] PROGMEM = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
static constexpr unsigned int BLINK2[] PROGMEM = {
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
static constexpr unsigned int BLINK3[] PROGMEM = {
		BLINK_DELAY_SHORT, BLINK_DELAY_LONG,
		0};

#endif
