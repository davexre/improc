#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"
#include "StateLed.h"

DefineClass(SteppingMotorTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static const int stepperPin11 = 7;	// BA6845FS Stepper motor driver OUT11 pin
static const int stepperPin12 = 8;	// BA6845FS Stepper motor driver OUT12 pin
static const int stepperPin21 = 9;	// BA6845FS Stepper motor driver OUT21 pin
static const int stepperPin22 = 10;	// BA6845FS Stepper motor driver OUT22 pin

static AdvButton btn;
static RotaryEncoderAcelleration rotor;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static void UpdateRotor() {
	rotor.update();
}

long lastRotor;
void SteppingMotorTest::setup() {
	btn.initialize(buttonPin, false);
	led.initialize(ledPin, states, size(states), true);
	toneState.setValue(500);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setState(&toneState);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(9600);
    Serial.println("Push the encoder button to switch between changing pitch and blink");

    ledState.initialize(0, size(states) - 1, true);
    toneState.initialize(50, 5000, false);
}


void SteppingMotorTest::loop() {
	btn.update();
	led.update();

	if (btn.isLongClicked()) {

	} else if (btn.isClicked()) {
		rotor.setState(rotor.getState() == &toneState ? &ledState : &toneState);
	}

	if (rotor.hasValueChanged()) {

	}
	if (toneState.hasValueChanged()) {
		long newTone = toneState.getValue();
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
