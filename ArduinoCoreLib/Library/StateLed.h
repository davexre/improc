#ifndef STATELED_H_
#define STATELED_H_

#include <wiring.h>
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
	const unsigned int *(*stateDelays); // All Delays AND the stateDelays array must be stored in PROGMEM

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
	void initialize(DigitalOutputPin *pin, const unsigned int *(*stateDelays),
			const short int numberOfStates, const bool looped = true);

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
	void setLooped(const bool looped);

	/**
	 * Sets the led blink sequence to the new state.
	 * If state is out of range then the accepted value is a
	 * modulus of the total number of states, i.e.
	 * acceptedState = state % numberOfStates;
	 */
	void setState(short int state);

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
