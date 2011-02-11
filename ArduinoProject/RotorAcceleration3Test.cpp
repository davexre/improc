#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "StateLed.h"
#include "RotorAcelleration3.h"

DefineClass(RotorAcceleration3Test);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin =  13;		// the number of the LED pin
static const int speakerPin = 8;
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static Button btn;
static StateLed led;
static boolean speakerOn = false;
static RotorAcelleration3 rotor;

static const unsigned int *states[] = {
		BLINK_FAST,
		BLINK_MEDIUM,
		BLINK_SLOW
};

void RotorAcceleration3Test::setup() {
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

void RotorAcceleration3Test::loop() {
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
