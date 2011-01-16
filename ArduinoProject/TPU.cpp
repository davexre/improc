//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "Button.h"
#include "RotorAcelleration.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int rotorPinA = 2;	// One quadrature pin
const int rotorPinB = 3;	// the other quadrature pin
const int speakerPin = 8;
const int ledPin = 13;

const int coilPins[] = { 5, 6, 7 };
const int coilCount = size(coilPins);

const byte coilStates1[][coilCount] = {
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
};

const byte coilStates2[][coilCount] = {
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 1, 1},
		{0, 0, 1},
		{1, 0, 1}
};

const byte coilStates[][coilCount] = {
		{1, 0, 0},
		{0, 0, 0},
		{0, 1, 0},
		{0, 0, 0},
		{0, 0, 1},
		{0, 0, 0}
};

const int coilStatesCount = size(coilStates);
int activeCoilState = 0;

Button btn;
boolean enabled = false;
boolean speakerState = false;

extern "C" void setup() {
	for (int i = 0; i < coilCount; i++) {
		pinMode(coilPins[i], OUTPUT);
		digitalWrite(coilPins[i], 0);
	}
	pinMode(speakerPin, OUTPUT);
	pinMode(ledPin, OUTPUT);
	digitalWrite(speakerPin, 0);
	digitalWrite(ledPin, 0);

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
		digitalWrite(ledPin, enabled);
		speakerState = false;
		digitalWrite(speakerPin, speakerPin);
	}

	if (enabled) {
		activeCoilState = (activeCoilState + 1) % coilStatesCount;
		const byte *states = coilStates[activeCoilState];
		for (int i = 0; i < coilCount; i++) {
			digitalWrite(coilPins[i], states[i]);
		}
		if (activeCoilState == 0)
			speakerState = !speakerState;
		digitalWrite(speakerPin, speakerState);
	} else {
		for (int i = 0; i < coilCount; i++) {
			digitalWrite(coilPins[i], 0);
		}
	}

	delay(curDelay);
}

#endif
