#include "BlinkingLed.h"

void BlinkingLed::update() {
	if (isPlaying()) {
		long cur = millis();
		if (cur - toggleTime >= 0) {
			unsigned int delta = getNextDelay();
			if (delta == 0) {
				lightOn = false;
			} else {
				lightOn = !lightOn;
			}
			toggleTime = cur + delta;
			digitalWrite(pin, lightOn ? HIGH : LOW);
		}
	} else {
		lightOn = false;
		digitalWrite(pin, LOW);
	}
}

void BlinkingLed::stop() {
	play(0);
}

void BlinkingLed::play(int playCount) {
	this->playCount = playCount;
	toggleTime = millis();
	update();
}

void BlinkingLed::initialilze(uint8_t pin) {
	this->pin = pin;
	this->delays = NULL;
	curDelay = 0;
	lightOn = false;
	playCount = 0;
	toggleTime = 0;
	pinMode(pin, OUTPUT);
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

void BlinkingLed::playBlink(const unsigned int *delays, int playCount) {
	uint8_t oldSREG = SREG;
	cli();
	curDelay = 0;
	this->delays = delays;
	this->playCount = playCount;
	lightOn = false;
	SREG = oldSREG;
	toggleTime = millis();
	update();
}
