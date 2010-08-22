//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "BlinkingLed.h"
#include "Button.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int ledPin = 13;		// the number of the LED pin

Button btn;
BlinkingLed led;

extern "C" void setup(void) {
	btn.initialize(buttonPin);
	led.initialize(ledPin);
}

extern "C" void loop(void) {
	btn.update();
	led.update();
	if (btn.isPressed()) {
		led.playBlink(BLINK2, 1);
	}
}

#endif
