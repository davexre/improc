#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "RotorAcelleration.h"

DefineClass(TPU);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int speakerPin = 8;
static const int ledPin = 13;

static const int coilPins[] = { 5, 6, 7 };
static const int coilCount = size(coilPins);

static const byte coilStates1[][coilCount] = {
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
};

static const byte coilStates2[][coilCount] = {
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 1, 1},
		{0, 0, 1},
		{1, 0, 1}
};

static const byte coilStates[][coilCount] = {
		{1, 0, 0},
		{0, 0, 0},
		{0, 1, 0},
		{0, 0, 0},
		{0, 0, 1},
		{0, 0, 0}
};

static const int coilStatesCount = size(coilStates);
static int activeCoilState = 0;

static Button btn;
static RotorAcelleration rotor;

static boolean enabled = false;
static boolean speakerState = false;

void TPU::setup() {
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
	rotor.setMinMax(0, 100);
}

void TPU::loop() {
	btn.update();
	rotor.update();

	long curDelay = rotor.getPosition();
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
