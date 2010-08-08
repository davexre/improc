#ifndef Button_h
#define Button_h

#include "WProgram.h"

void delayLoop(unsigned long millis);

/**
 *  The cirtuit: push button attached to pin_X from ground
 */
class Button {
public:
	int buttonPin;
	int lastState;
	int buttonState;

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
