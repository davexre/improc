#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"
#include "StateLed.h"

DefineClass(RotaryEncoderAcellerationTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static AdvButton btn;
static boolean speakerOn = true;
static RotaryEncoderAcelleration rotor;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

RotaryEncoderState ledState = RotaryEncoderState(0, size(states), true);
RotaryEncoderState toneState = RotaryEncoderState(50, 5000, false);

void UpdateRotor() {
	rotor.update();
}

void RotaryEncoderAcellerationTest::setup() {
	pinMode(speakerPin, OUTPUT);
	btn.initialize(buttonPin);
	led.initialize(ledPin, size(states), states, true);
	toneState.setValue(500);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setState(&toneState);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(9600);
    Serial.println("Push the encoder button to switch between changing pitch and blink");
}

void RotaryEncoderAcellerationTest::loop() {
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, toneState.getValue());
		} else {
			noTone(speakerPin);
		}
	} else if (btn.isClicked()) {
		rotor.setState(rotor.getState() == &toneState ? &ledState : &toneState);
	}

	if (toneState.hasValueChanged()) {
		long newTone = toneState.getValue();
		if (speakerOn) {
			tone(speakerPin, newTone);
		}
		float tps = rotor.tps.getTPS();
		Serial.print("Tone ");
		Serial.print(newTone);
		Serial.print(" ");
		Serial.println(tps);
	}

	if (ledState.hasValueChanged()) {
		int newLed = (int) ledState.getValue();
		led.setState(newLed);
		Serial.print("Led ");
		Serial.println(newLed);
	}
}
