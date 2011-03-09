#include "StateLed.h"

void StateLed::initialize(uint8_t pin, const unsigned int *(*stateDelays),
		short int numberOfStates, boolean looped) {
	this->looped = looped;
	this->numberOfStates = numberOfStates;
	this->stateDelays = stateDelays;
	led.initialize(pin);
	setState(0);
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
