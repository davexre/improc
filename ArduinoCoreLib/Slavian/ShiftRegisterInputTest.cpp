#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "DigitalIO.h"

DefineClass(ShiftRegisterInputTest);

static const int ledPin = 6; // the number of the LED pin

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;

static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static DigitalInputShiftRegister_74HC166 shiftRegisterInput;
static DigitalOutputArduinoPin diLedPin;
static DigitalOutputArduinoPin diShiftRegisterInputPinPE;
static DigitalOutputArduinoPin diShiftRegisterInputPinCP;
static DigitalInputArduinoPin diShiftRegisterInputPinQ7;

#define DigitalInputShiftRegisterPinsCount 9

void ShiftRegisterInputTest::setup() {
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	diShiftRegisterInputPinPE.initialize(shiftRegisterInputPinCP, false);
	diShiftRegisterInputPinCP.initialize(shiftRegisterInputPinCP, false);
	diShiftRegisterInputPinQ7.initialize(shiftRegisterInputPinQ7, false);
	shiftRegisterInput.initialize(DigitalInputShiftRegisterPinsCount,
			&diShiftRegisterInputPinPE,
			&diShiftRegisterInputPinCP,
			&diShiftRegisterInputPinQ7);

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
