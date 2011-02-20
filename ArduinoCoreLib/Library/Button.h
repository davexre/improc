#ifndef Button_h
#define Button_h

#include "WProgram.h"

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
	uint8_t buttonPin;
	boolean lastState;
	boolean buttonState;
	long lastToggleTime;	// used to debounce the button

	/**
	 * Debounce time in milliseconds, default 10
	 */
	int debounce;
public:
	void initialize(uint8_t pin, int debounceMillis = 10);
	void update(void);

	inline boolean isPressed(void) {
		return ((!buttonState) && lastState);
	}

	inline boolean isReleased(void) {
		return (buttonState && (!lastState));
	}

	inline boolean isDown(void) {
		return (!buttonState);
	}

	inline boolean isUp(void) {
		return (buttonState);
	}
};

#endif
