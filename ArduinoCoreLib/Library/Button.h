#ifndef Button_h
#define Button_h

#include "DigitalIO.h"
#include <wiring.h>

/**
 * A debounced button class.
 *
 * The debouncing is done via tracking the time of last
 * button toggle and not by delaying. This button class
 * uses the ATMEGA's internal pull-up resistors.
 *
 * The circuit: push button attached to pin_X from ground
 * http://www.arduino.cc/en/Tutorial/Debounce
 *
 * Optional components to hardware debounce a button:
 * http://www.ganssle.com/debouncing-pt2.htm
 *
 *     (internal 20k)       10k
 * pin:<---/\/\--------*----/\/\----|
 *                     |            |
 *              0.1uf ===            / switch
 *                     |            /
 * gnd:<---------------*------------|
 */
class Button {
	DigitalInputPin *buttonPin;
	bool lastState;
	bool currentState;
	unsigned long lastToggleTime;	// used to debounce the button

	/**
	 * Debounce time in milliseconds, default 10
	 */
	unsigned int debounce;

public:
	/**
	 * Initializes the class.
	 *
	 * pin		Number of the pin there the button is attached.
	 *
	 * debounceMillis
	 * 			The button debounce time in milliseconds.
	 */
	void initialize(DigitalInputPin *pin, const unsigned int debounceMillis = 10);

	/**
	 * Updates the state of the rotary knob.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update(void);

	/**
	 * Has the button stated changed from isUp to isDown at the last update.
	 * This is to be used like an OnKeyDown.
	 */
	inline bool isPressed(void) {
		return ((!currentState) && lastState);
	}

	/**
	 * Has the button stated changed from isDown to isUp at the last update.
	 * This is to be used like an OnKeyUp.
	 */
	inline bool isReleased(void) {
		return (currentState && (!lastState));
	}

	/**
	 * Is the button down (pushed).
	 */
	inline bool isDown(void) {
		return (!currentState);
	}

	/**
	 * Is the button up.
	 */
	inline bool isUp(void) {
		return (currentState);
	}

	/**
	 * Has the state changed from up to down or vice versa.
	 */
	inline bool isToggled(void) {
		return (currentState != lastState);
	}

	void reset();
};

#endif
