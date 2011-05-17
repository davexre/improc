#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "ShiftRegisterInput.h"

DefineClass(ShiftRegisterInputTest);

static const int ledPin = 13; // the number of the LED pin

static const int shiftRegisterInputPinPE = 8;
static const int shiftRegisterInputPinCP = 9;
static const int shiftRegisterInputPinQ7 = 10;

static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static ShiftRegisterInput shiftRegisterInput;

void ShiftRegisterInputTest::setup() {
	led.initialize(ledPin, states, size(states), true);

	shiftRegisterInput.initialize(shiftRegisterInputPinPE, shiftRegisterInputPinCP, shiftRegisterInputPinQ7);

    Serial.begin(115200);
    Serial.println("Initialized");
}

boolean prevBuffer[ShiftRegisterInputPinsCount];

void ShiftRegisterInputTest::loop() {
	led.update();
	shiftRegisterInput.update();

	boolean show = false;
	for (int i = 0; i < ShiftRegisterInputPinsCount; i++) {
		boolean val = shiftRegisterInput.getState(i);
		if (val != prevBuffer[i]) {
			show = true;
			prevBuffer[i] = val;
		}
	}

	if (show) {
		for (int i = 0; i < ShiftRegisterInputPinsCount; i++) {
			boolean val = shiftRegisterInput.getState(i);
			Serial.print(val ? '1' : '0');
			if (i % 4 == 3)
				Serial.print(' ');
		}
		Serial.println();
	}
}
