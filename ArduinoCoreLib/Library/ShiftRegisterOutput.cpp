#include "ShiftRegisterOutput.h"

void ShiftRegisterOutput::initialize(const uint8_t CP_pin, const uint8_t DS_pin) {
	this->CP_pin = CP_pin;
	this->DS_pin = DS_pin;
	modified = true;

	pinMode(CP_pin, OUTPUT);
	digitalWrite(CP_pin, LOW);

	pinMode(DS_pin, OUTPUT);
	digitalWrite(DS_pin, LOW);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
}

boolean ShiftRegisterOutput::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= ShiftRegisterOutputPinsCount ? 0 :
		buffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

void ShiftRegisterOutput::setState(const uint8_t shiftRegisterPin, const boolean value) {
	if (shiftRegisterPin < ShiftRegisterOutputPinsCount) {
		uint8_t mask = 1 << (shiftRegisterPin & 0b0111);
		uint8_t *buf = &buffer[shiftRegisterPin >> 3];
		if ((*buf & mask) ^ (value)) {
			modified = true;
			if (value)
				*buf |= mask;
			else
				*buf &= ~mask;
		}
	}
}

void ShiftRegisterOutput::update() {
	if (modified) {
		uint8_t mask = 1;
		uint8_t *buf = buffer;
		for (uint8_t i = 0; i < ShiftRegisterOutputPinsCount; i++) {
			digitalWrite(CP_pin, LOW);
			digitalWrite(DS_pin, *buf & mask);
			digitalWrite(CP_pin, HIGH);
			mask <<= 1;
			if (mask == 0) {
				buf++;
				mask = 1;
			}
		}
		digitalWrite(CP_pin, LOW);
		digitalWrite(DS_pin, LOW);
	}
}
