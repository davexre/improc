#ifndef Button_h
#define Button_h

#include "WProgram.h"

/**
 *  The cirtuit: push button attached to pin_X from ground
 */
class Button {
public:
	int buttonPin;
	int lastState;
	int buttonState;
	int debounce;		// time in millis, default 10
	long lastTime;		// used to debounce the button, so no delay is needed

	void initialize(int pin);
	void update(void);

	inline boolean isPressed(void) {
		return ((buttonState == LOW) && (lastState == HIGH));
	}

	inline boolean isReleased(void) {
		return ((buttonState == HIGH) && (lastState == LOW));
	}

	inline boolean isDown(void) {
		return (buttonState == LOW);
	}

	inline boolean isUp(void) {
		return (buttonState == HIGH);
	}
};

#endif
