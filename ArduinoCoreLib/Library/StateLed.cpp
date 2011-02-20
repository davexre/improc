#include "StateLed.h"

void StateLed::initialize(uint8_t pin, short int numberOfStates,
		const unsigned int *(*stateDelays), boolean looped) {
	this->looped = looped;
	this->numberOfStates = numberOfStates;
	this->stateDelays = stateDelays;
	state = 0;
	led.initialize(pin);
	led.playBlink(stateDelays[state], 0);
}

void StateLed::setState(short int state) {
	if (state >= numberOfStates)
		state %= numberOfStates;
	if (state < 0)
		state = numberOfStates - 1 + state % numberOfStates;
	if ((this->state != state) || (!led.isPlaying())) {
		this->state = state;
		led.playBlink(stateDelays[state], looped ? -1 : 1);
	}
}

void StateLed::setLooped(boolean looped) {
	if (this->looped != looped) {
		this->looped = looped;
		led.playBlink(stateDelays[state], looped ? -1 : 1);
	}
}
