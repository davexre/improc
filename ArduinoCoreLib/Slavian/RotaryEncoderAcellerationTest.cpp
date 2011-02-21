#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "RotaryEncoderAcelleration.h"

DefineClass(RotaryEncoderAcellerationTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static Button btn;
static boolean speakerOn = true;
static RotaryEncoderAcelleration rotor;

void UpdateRotor() {
	rotor.update();
}

void RotaryEncoderAcellerationTest::setup() {
	pinMode(speakerPin, OUTPUT);
	btn.initialize(buttonPin);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setMinMax(0, 5000);
	rotor.setPosition(500);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(9600);
}

long lastRotor = 0;
void RotaryEncoderAcellerationTest::loop() {
	btn.update();

	long pos = rotor.getPosition();
	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, pos);
		} else {
			noTone(speakerPin);
		}
	}

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
