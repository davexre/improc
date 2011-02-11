#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "Button.h"
#include "StateLed.h"
#include "RotorAcelleration3.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int ledPin =  13;		// the number of the LED pin
const int speakerPin = 8;
const int rotorPinA = 2;	// One quadrature pin
const int rotorPinB = 3;	// the other quadrature pin

Button btn;
StateLed led;
boolean speakerOn = false;
RotorAcelleration3 rotor;

const unsigned int *states[] = {
		BLINK_FAST,
		BLINK_MEDIUM,
		BLINK_SLOW
};

extern "C" void setup() {
	pinMode(speakerPin, OUTPUT);

	btn.initialize(buttonPin);
	led.initialize(ledPin, true, size(states), states);
	led.setState(1);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.minValue = 0;
	rotor.maxValue = 50000;
	rotor.position = 500;

    Serial.begin(9600);
}

extern "C" void loop() {
	btn.update();
	led.update();
	rotor.update();

	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, rotor.position);
			led.setState(0);
		} else {
			noTone(speakerPin);
			led.setState(1);
		}
	}

	if (rotor.isTicked()) {
		if (speakerOn) {
			tone(speakerPin, rotor.position);
		}
		Serial.println(rotor.position);
	}
}

#endif
