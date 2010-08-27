#ifndef STATELED_H_
#define STATELED_H_

#include "BlinkingLed.h"

/**
 * Show different states via blinking a led.
 * Uses timer0 via the millis() function.
 *
 * Example:
 *
 * StateLed led;
 * const unsigned int *states[] = {
 * 		BLINK1, BLINK2
 * };
 *
 * ...
 * led.initialize(pin, true, size(states), states);
 *
 */
class StateLed {
public:
	BlinkingLed led;
	boolean looped;
	int state;
	int numberOfStates;
	const unsigned int *(*stateDelays);

	void initialize(uint8_t pin, boolean looped, int numberOfStates, const unsigned int *(*stateDelays));
	void setState(int state);
	void nextState(void);
	void previousState(void);
	void update(void);
	void setLooped(boolean looped);
};

#endif
