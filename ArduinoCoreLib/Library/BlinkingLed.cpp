#include "BlinkingLed.h"
#include "utils.h"

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
		unsigned long cur = millis();
		if (cur - toggleTime >= 0) {
			unsigned int delta = getNextDelay();
			if (delta == 0) {
				lightOn = false;
			} else {
				lightOn = !lightOn;
			}
			toggleTime = cur + delta;
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
		result = delays[curDelay++];
		if (result == 0) {
			curDelay = 0;
			if (playCount > 0)
				playCount--;
			result = delays[curDelay++];
			if (result == 0) {
				curDelay = 0;
				playCount = 0;
			}
			lightOn = false;
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
