#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "StateLed.h"
#include "RotorAcelleration.h"

DefineClass(RotorAccelerationTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin =  13;		// the number of the LED pin
static const int speakerPin = 8;
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static Button btn;
static StateLed led;
static boolean speakerOn = false;
static RotorAcelleration rotor;

static const unsigned int *states[] = {
		BLINK_FAST,
		BLINK_MEDIUM,
		BLINK_SLOW
};

void UpdateRotor() {
	rotor.update();
}

void RotorAccelerationTest::setup() {
	pinMode(speakerPin, OUTPUT);

	btn.initialize(buttonPin);
	led.initialize(ledPin, true, size(states), states);
	led.setState(1);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setMinMax(0, 5000);
	rotor.setPosition(500);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(9600);
}

long lastRotor = 0;
void RotorAccelerationTest::loop() {
	btn.update();
	led.update();
//	rotor.update();

	long pos = rotor.getPosition();
	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, pos);
			led.setState(0);
		} else {
			noTone(speakerPin);
			led.setState(1);
		}
	}

//	if (rotor.isTicked()) {
	if (lastRotor != pos) {
		if (speakerOn) {
			tone(speakerPin, pos);
		}
		float tps = rotor.tps.getTPS();
		Serial.print(pos);
		Serial.print(" ");
		Serial.println(tps);
	}
	lastRotor = pos;
}
