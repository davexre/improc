//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "StateLed.h"
#include "Button.h"

const int buttonPin = 4; // the number of the pushbutton pin
const int ledPin = 13; // the number of the LED pin

Button btn;
StateLed led;

const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3,
		BLINK_ON, BLINK_OFF
};

extern "C" void setup(void) {
	btn.initialize(buttonPin);
	led.initialize(ledPin, true, size(states), states);
}

extern "C" void loop(void) {
	btn.update();
	led.update();
	if (btn.isPressed())
		led.nextState();
}

#endif
