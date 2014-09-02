#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "DigitalIO.h"

DefineClass(ShiftRegisterInputTest2);

static const int ledPin = 6; // the number of the LED pin

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;

static const unsigned int *const states[] PROGMEM = {
	BLINK_SLOW,
	BLINK_MEDIUM,
	BLINK_OFF,
	BLINK1, BLINK2, BLINK3
};

StateLed led;

DigitalInputShiftRegister_74HC166<> shiftRegisterInput;
DigitalOutputArduinoPin diLedPin;
DigitalOutputArduinoPin diShiftRegisterInputPinPE;
DigitalOutputArduinoPin diShiftRegisterInputPinCP;
DigitalInputArduinoPin diShiftRegisterInputPinQ7;

static const int DigitalInputShiftRegisterPinsCount = 9;
bool prevBuffer[DigitalInputShiftRegisterPinsCount];

void ShiftRegisterInputTest2::setup() {
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
	Serial_println("Initialized");
}

void ShiftRegisterInputTest2::loop() {
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
