#ifndef Button_h
#define Button_h

#include "WProgram.h"

/**
 *  The cirtuit: push button attached to pin_X from ground
 *
 *  http://www.arduino.cc/en/Tutorial/Debounce
 */
class Button {
public:
	uint8_t buttonPin;
	boolean lastState;
	boolean buttonState;
	boolean lastPortReading;
	int debounce;			// time in millis, default 10
	long lastToggleTime;	// used to debounce the button, so no delay is needed

	void initialize(uint8_t pin);
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
