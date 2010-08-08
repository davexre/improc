//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "Button.h"

const int buttonPin = 4; // the number of the pushbutton pin
const int ledPin = 13; // the number of the LED pin

Button btn;
boolean lightOn = false;

extern "C" void setup() {
	pinMode(ledPin, OUTPUT);
	btn.initialize(buttonPin);
}

extern "C" void loop() {
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
	delayLoop(50);
}

#endif
