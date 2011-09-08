#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "AdvButton.h"
#include "DigitalIO.h"
#include "RotaryEncoderAcceleration.h"

DefineClass(ShiftRegisterOutputTest2);

static const int ledPin = 6; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4; // the number of the pushbutton pin

static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;
static const int shiftRegisterOutputPinDS = 11;

static RotaryEncoderAcceleration rotor;

static StateLed led;
static AdvButton btn;

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

#define DigitalOutputShiftRegisterPinsCount 16

static DigitalOutputShiftRegister_74HC595 extenderOut;

void ShiftRegisterOutputTest2::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);

	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setMinMax(-1, 15);
	rotor.setValue(-1);
	attachInterrupt(0, UpdateRotor, CHANGE);

	extenderOut.initialize(DigitalOutputShiftRegisterPinsCount,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));

	for (int i = 0; i < 16; i++) {
		extenderOut.setState(i, false);
	}

    Serial.begin(115200);
    Serial.println("Initialized");
}

bool pinsOn = false;
void ShiftRegisterOutputTest2::loop() {
	btn.update();
	led.update();
	extenderOut.update();

	if (rotor.hasValueChanged()) {
		int curState = rotor.getValue();
		for (int i = 0; i < 16; i++) {
			extenderOut.setState(i, i == curState);
		}
		Serial.println(curState);
	}
}
