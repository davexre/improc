#include "ShiftRegisterInput.h"

void ShiftRegisterInput::initialize(const uint8_t PE_pin, const uint8_t CP_pin, const uint8_t Q7_pin) {
	this->PE_pin = PE_pin;
	this->CP_pin = CP_pin;
	this->Q7_pin = Q7_pin;

	pinMode(PE_pin, OUTPUT);
	digitalWrite(PE_pin, LOW);

	pinMode(CP_pin, OUTPUT);
	digitalWrite(CP_pin, LOW);

	pinMode(Q7_pin, INPUT);
	digitalWrite(Q7_pin, LOW); // disable internal pull up resistor

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
}

boolean ShiftRegisterInput::getState(const uint8_t shifRegisterPin) {
	return shifRegisterPin >= ShiftRegisterInputPinsCount ? 0 :
		buffer[shifRegisterPin >> 3] & (1 << (shifRegisterPin & 0b0111));
}

void ShiftRegisterInput::update() {
	// Load data into register
	digitalWrite(CP_pin, LOW);
	digitalWrite(PE_pin, LOW);
	digitalWrite(CP_pin, HIGH);

	// Start reading
	digitalWrite(PE_pin, HIGH);
	uint8_t mask = 1;
	uint8_t *buf = buffer;
	for (uint8_t i = 0; i < ShiftRegisterInputPinsCount; i++) {
		if (digitalRead(Q7_pin)) {
			*buf |= mask;
		} else {
			*buf &= ~mask;
		}
		digitalWrite(CP_pin, LOW);
		digitalWrite(CP_pin, HIGH);
		mask <<= 1;
		if (mask == 0)
			buf++;
	}

	digitalWrite(PE_pin, LOW);
	digitalWrite(CP_pin, LOW);
}
