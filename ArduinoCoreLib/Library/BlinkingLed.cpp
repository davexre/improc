#include "BlinkingLed.h"
#include "avr/pgmspace.h"
#include "utils.h"

const unsigned int PROGMEM BLINK_SLOW[] = {
		BLINK_DELAY_LONG, BLINK_DELAY_LONG,
		0};
const unsigned int PROGMEM BLINK_MEDIUM[] = {
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK_FAST[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		0};
const unsigned int PROGMEM BLINK_OFF[] = {0};
const unsigned int PROGMEM BLINK_ON[] = {BLINK_DELAY_SHORT, 0};

const unsigned int PROGMEM BLINK1[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK2[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK3[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_LONG,
		0};

void BlinkingLed::initialize(DigitalOutputPin *pin) {
	this->pin = pin;
	this->delays = NULL;
	curDelay = 0;
	lightOn = false;
	playCount = 0;
	toggleTime = 0;
}

void BlinkingLed::update() {
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

void BlinkingLed::play(const signed short int playCount) {
	if (delays != NULL) {
		if (!isPlaying()) {
			toggleTime = millis();
		}
		this->playCount = playCount;
		update();
	}
}

unsigned int BlinkingLed::getNextDelay() {
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

void BlinkingLed::playBlink(const unsigned int *delays, signed short int playCount) {
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
