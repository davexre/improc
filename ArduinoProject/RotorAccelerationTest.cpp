//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "Button.h"
#include "StateLed.h"
#include "RotorAcelleration.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int ledPin =  12;		// the number of the LED pin
const int speakerPin = 8;
const int rotorPinA = 2;	// One quadrature pin
const int rotorPinB = 3;	// the other quadrature pin

Button btn;
StateLed led;
boolean speakerOn = false;

const unsigned int *states[] = {
		BLINK_FAST,
		BLINK_MEDIUM,
		BLINK_SLOW
};


long lastRotor;

extern "C" void setup() {
	pinMode(speakerPin, OUTPUT);

	btn.initialize(buttonPin);
	led.initialilze(ledPin, true, size(states), states);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.minValue = 0;
	rotor.maxValue = 50000;
	lastRotor = rotor.position = 500;

    Serial.begin(9600);
}

extern "C" void loop() {
	btn.update();
	led.update();
	rotor.update();

	long pitch = rotor.position;
	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, pitch);
			led.setState(0);
		} else {
			noTone(speakerPin);
			led.setState(1);
		}
	}

	if (pitch != lastRotor) {
		if (speakerOn) {
			tone(speakerPin, pitch);
		}
		Serial.println(pitch);
		lastRotor = pitch;
	}
	delay(10);
}

#endif
