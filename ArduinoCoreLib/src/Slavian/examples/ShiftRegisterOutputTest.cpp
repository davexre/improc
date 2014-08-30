#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "AdvButton.h"
#include "DigitalIO.h"

DefineClass(ShiftRegisterOutputTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static const int shiftRegisterOutputPinCP = 8;
static const int shiftRegisterOutputPinDS = 9;

static StateLed led;
static AdvButton btn;

static const unsigned int *const states[] PROGMEM = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static DigitalOutputShiftRegister_74HC164 shiftRegisterOutput;
static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;
static DigitalOutputArduinoPin diShiftRegisterOutputPinCP;
static DigitalOutputArduinoPin diShiftRegisterOutputPinDS;

#define DigitalOutputShiftRegisterPinsCount 16

void ShiftRegisterOutputTest::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	diShiftRegisterOutputPinCP.initialize(shiftRegisterOutputPinCP, 0);
	diShiftRegisterOutputPinDS.initialize(shiftRegisterOutputPinDS, 0);
	shiftRegisterOutput.initialize(DigitalOutputShiftRegisterPinsCount,
			&diShiftRegisterOutputPinCP,
			&diShiftRegisterOutputPinDS);

    Serial.begin(115200);
    Serial.println("Initialized");
}

bool pinsOn = false;
void ShiftRegisterOutputTest::loop() {
	btn.update();
	led.update();
	shiftRegisterOutput.update();

	if (btn.isDoubleClicked()) {
		pinsOn = !pinsOn;
		for (int i = 0; i < DigitalOutputShiftRegisterPinsCount; i++) {
			shiftRegisterOutput.setState(i, (i & 1) && pinsOn);
		}
	} else if (btn.isClicked()) {
		for (int i = 0; i < DigitalOutputShiftRegisterPinsCount; i++) {
			shiftRegisterOutput.setState(i, !shiftRegisterOutput.getState(i));
		}
	}
}
