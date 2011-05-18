#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "AdvButton.h"
#include "ShiftRegisterOutput.h"

DefineClass(ShiftRegisterOutputTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static const int shiftRegisterOutputPinCP = 8;
static const int shiftRegisterOutputPinDS = 9;

static StateLed led;
static AdvButton btn;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static ShiftRegisterOutput shiftRegisterOutput;

void ShiftRegisterOutputTest::setup() {
	btn.initialize(buttonPin, false);
	led.initialize(ledPin, states, size(states), true);

	shiftRegisterOutput.initialize(shiftRegisterOutputPinCP, shiftRegisterOutputPinDS);

    Serial.begin(115200);
    Serial.println("Initialized");
}

boolean pinsOn = false;
void ShiftRegisterOutputTest::loop() {
	btn.update();
	led.update();
	shiftRegisterOutput.update();

	if (btn.isDoubleClicked()) {
		pinsOn = !pinsOn;
		for (int i = 0; i < ShiftRegisterOutputPinsCount; i++) {
			shiftRegisterOutput.setState(i, (i & 1) && pinsOn);
		}
	} else if (btn.isClicked()) {
		for (int i = 0; i < ShiftRegisterOutputPinsCount; i++) {
			shiftRegisterOutput.setState(i, !shiftRegisterOutput.getState(i));
		}
	}
}
