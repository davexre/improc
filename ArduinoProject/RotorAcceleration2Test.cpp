#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "StateLed.h"
#include "RotorAcelleration2.h"

DefineClass(RotorAcceleration2Test);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin =  13;		// the number of the LED pin
static const int speakerPin = 8;
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static Button btn;
static StateLed led;
static boolean speakerOn = false;
static RotorAcelleration2 rotor;

static const unsigned int *states[] = {
		BLINK_FAST,
		BLINK_MEDIUM,
		BLINK_SLOW
};

static long lastRotor;

void RotorAcceleration2Test::setup() {
	pinMode(speakerPin, OUTPUT);

	btn.initialize(buttonPin);
	led.initialize(ledPin, true, size(states), states);
	led.setState(1);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.minValue = 0;
	rotor.maxValue = 50000;
	lastRotor = rotor.position = 500;

    Serial.begin(9600);
}

void RotorAcceleration2Test::loop() {
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
}
