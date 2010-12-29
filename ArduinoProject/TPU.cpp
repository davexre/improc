//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "Button.h"
#include "RotorAcelleration.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int rotorPinA = 2;	// One quadrature pin
const int rotorPinB = 3;	// the other quadrature pin

const int coilPins[] = { 5, 6, 7 };
const int coilCount = size(coilPins);
int activeCoil = 0;

Button btn;
boolean enabled = false;

extern "C" void setup() {
	for (int i = 0; i < coilCount; i++) {
		pinMode(coilPins[i], OUTPUT);
		digitalWrite(coilPins[i], 0);
	}
	btn.initialize(buttonPin);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.steps[0] = 1;
	rotor.steps[1] = 1;
	rotor.steps[2] = 1;

	rotor.minValue = 0;
	rotor.maxValue = 100;
}

extern "C" void loop() {
	btn.update();
	rotor.update();

	long curDelay = rotor.position;
	if (btn.isPressed()) {
		enabled = !enabled;
	}

	if (enabled) {
		activeCoil = (activeCoil + 1) % coilCount;
		for (int i = 0; i < coilCount; i++) {
			digitalWrite(coilPins[i], i == activeCoil);
		}
	} else {
		for (int i = 0; i < coilCount; i++) {
			digitalWrite(coilPins[i], 0);
		}
	}

	delay(curDelay);
}

#endif
