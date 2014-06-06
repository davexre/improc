#ifndef STATELED_H_
#define STATELED_H_

#include <BlinkingLed.h>

/**
 * Show different states via blinking a led.
 * Uses timer0 via the millis() function.
 *
 * Example:
 *
 * StateLed led;
 * const unsigned int PROGMEM *states[] = {
 * 		BLINK1, BLINK2
 * };
 *
 * ...
 * led.initialize(pin, size(states), states, true);
 *
 */
class StateLed {
private:
	BlinkingLed led;

	/**
	 * If looped=false then a state will be shown by a single playing
	 * of the appropriate blink sequence after a call to setState(),
	 * nextState(), previousState().
	 */
	bool looped;

	/**
	 * The currently set state.
	 */
	short int state;

	/**
	 * Number of blinking led states.
	 */
	short int numberOfStates;

	/**
	 * Led on/off blink sequences. See the example above.
	 */
	const unsigned int *const (*stateDelays); // All Delays AND the stateDelays array must be stored in PROGMEM

public:
	/**
	 * Initializes the class.
	 *
	 * pin	The pin number the led is attached to.
	 *
	 * numberOfStates
	 * 		Number of led states passed in the stateDelays.
	 *
	 * stateDelays
	 * 		Led on/off blink sequences. See the example above.
	 *
	 * looped
	 * 		Specifies how states will be shown - a single blink
	 * 		or via a continuous blinking loop.
	 */
	void initialize(DigitalOutputPin *pin, const unsigned int *const (*stateDelays),
			const short int numberOfStates, const bool looped = true) {
		this->looped = looped;
		this->numberOfStates = numberOfStates;
		this->stateDelays = stateDelays;
		led.initialize(pin);
		setState(0);
	}

	/**
	 * Updates the on/off state of the led. This method should be
	 * placed in the main loop of the program or might be invoked
	 * from an interrupt.
	 */
	inline void update(void) {
		led.update();
	}

	/**
	 * Sets looped state of the blinking led.
	 */
	void setLooped(const bool looped) {
		if (this->looped != looped) {
			this->looped = looped;
			led.playBlink(stateDelays[state], looped ? -1 : 1);
		}
	}

	/**
	 * Sets the led blink sequence to the new state.
	 * If state is out of range then the accepted value is a
	 * modulus of the total number of states, i.e.
	 * acceptedState = state % numberOfStates;
	 */
	void setState(short int state) {
		if (state >= numberOfStates)
			state %= numberOfStates;
		if (state < 0)
			state = numberOfStates - 1 + state % numberOfStates;
		if ((this->state != state) || (!led.isPlaying())) {
			this->state = state;
			led.playBlink((unsigned int *)pgm_read_word(&(stateDelays[state])), looped ? -1 : 1);
		}
	}

	/**
	 * Starts playing the next state.
	 */
	inline void nextState(void) {
		setState(state + 1);
	}

	/**
	 * Starts playing the previous state.
	 */
	inline void previousState(void) {
		setState(state - 1);
	}

	/**
	 * Returns the state of the led.
	 */
	inline bool isLedOn() {
		return (led.isLedOn());
	}
};

#endif
