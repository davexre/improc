#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "DigitalIO.h"

DefineClass(ShiftRegisterInputTest);

static const int ledPin = 6; // the number of the LED pin

static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinQ7 = 10;

static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static DigitalInputShiftRegister shiftRegisterInput;

void ShiftRegisterInputTest::setup() {
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);

	shiftRegisterInput.initialize(
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

    Serial.begin(115200);
    Serial.println("Initialized");
}

bool prevBuffer[DigitalInputShiftRegisterPinsCount];

void ShiftRegisterInputTest::loop() {
	led.update();
	shiftRegisterInput.update();

	bool show = false;
	for (int i = 0; i < DigitalInputShiftRegisterPinsCount; i++) {
		bool val = shiftRegisterInput.getState(i);
		if (val != prevBuffer[i]) {
			show = true;
			prevBuffer[i] = val;
		}
	}

	if (show) {
		for (int i = 0; i < DigitalInputShiftRegisterPinsCount; i++) {
			bool val = shiftRegisterInput.getState(i);
			Serial.print(val ? '1' : '0');
			if (i % 4 == 3)
				Serial.print(' ');
		}
		Serial.println();
	}
}
