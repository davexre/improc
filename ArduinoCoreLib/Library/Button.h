#ifndef Button_h
#define Button_h

#include "WProgram.h"

/**
 *  The cirtuit: push button attached to pin_X from ground
 *  http://www.arduino.cc/en/Tutorial/Debounce
 *
 *  Optional components to hardware debounce a button:
 *  http://www.ganssle.com/debouncing-pt2.htm
 *
 *      (internal 20k)       10k
 *  pin:<---/\/\--------*----/\/\----|
 *                      |            |
 *               0.1uf ===            / switch
 *                      |            /
 *  gnd:<---------------*------------|
 */
class Button {
	uint8_t buttonPin;
	boolean lastState;
	boolean buttonState;
	boolean lastPortReading;
	long lastToggleTime;	// used to debounce the button, so no delay is needed
public:
	int debounce;			// time in millis, default 10

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
