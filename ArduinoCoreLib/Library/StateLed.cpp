#include "StateLed.h"

void StateLed::initialize(uint8_t pin, boolean looped, int numberOfStates, const unsigned int *(*stateDelays)) {
	this->looped = looped;
	this->numberOfStates = numberOfStates;
	this->stateDelays = stateDelays;
	led.initialize(pin);
	state = 0;
	led.playBlink(stateDelays[state], 0);
}

void StateLed::setState(int state) {
	if (state >= numberOfStates)
		state %= numberOfStates;
	if (state < 0)
		state = numberOfStates - 1 + state % numberOfStates;
	if ((this->state != state) || (!led.isPlaying())) {
		this->state = state;
		led.playBlink(stateDelays[state], looped ? -1 : 1);
	}
}

void StateLed::nextState(void) {
	setState(state + 1);
}

void StateLed::previousState(void) {
	setState(state - 1);
}

void StateLed::setLooped(boolean looped) {
	if (this->looped != looped) {
		this->looped = looped;
		led.playBlink(stateDelays[state], looped ? -1 : 1);
	}
}
