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

static const unsigned int *const states[] PROGMEM = {
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

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;
static DigitalOutputArduinoPin diShiftRegisterOutputPinSH;
static DigitalOutputArduinoPin diShiftRegisterOutputPinST;
static DigitalOutputArduinoPin diShiftRegisterOutputPinDS;

static DigitalOutputShiftRegister_74HC595 extenderOut;

void ShiftRegisterOutputTest2::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	rotor.setMinMax(-1, 15);
	rotor.setValue(-1);
	attachInterrupt(0, UpdateRotor, CHANGE);

	diShiftRegisterOutputPinSH.initialize(shiftRegisterOutputPinSH, false);
	diShiftRegisterOutputPinST.initialize(shiftRegisterOutputPinST, false);
	diShiftRegisterOutputPinDS.initialize(shiftRegisterOutputPinDS, false);
	extenderOut.initialize(DigitalOutputShiftRegisterPinsCount, DigitalOutputShiftRegister_74HC595::BeforeWriteZeroAllOutputs,
			&diShiftRegisterOutputPinSH,
			&diShiftRegisterOutputPinST,
			&diShiftRegisterOutputPinDS);

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
