#include "DigitalIO.h"
#include "utils.h"

///////// DigitalInputArduinoPin

DigitalInputArduinoPin::DigitalInputArduinoPin(const uint8_t arduinoPin, const boolean enablePullup) {
	bit = digitalPinToBitMask(arduinoPin);
	uint8_t port = digitalPinToPort(arduinoPin);
	inputRegister = portInputRegister(port);
	pinMode(arduinoPin, INPUT);

	volatile uint8_t *outputRegister = portOutputRegister(port);
	if (enablePullup) {
		disableInterrupts();
		*outputRegister |= bit;
		restoreInterrupts();
	} else {
		disableInterrupts();
		*outputRegister &= ~bit;
		restoreInterrupts();
	}
}

boolean DigitalInputArduinoPin::getState() {
	return (*inputRegister & bit);
}

///////// DigitalOutputArduinoPin

DigitalOutputArduinoPin::DigitalOutputArduinoPin(const uint8_t arduinoPin, const boolean initialValue) {
	bit = digitalPinToBitMask(arduinoPin);
	uint8_t port = digitalPinToPort(arduinoPin);
	outputRegister = portOutputRegister(port);
	pinMode(arduinoPin, INPUT);
	setState(initialValue);
}

boolean DigitalOutputArduinoPin::getState() {
	return lastState;
}

void DigitalOutputArduinoPin::setState(const boolean value) {
	lastState = value;
	if (value) {
		disableInterrupts();
		*outputRegister |= bit;
		restoreInterrupts();
	} else {
		disableInterrupts();
		*outputRegister &= ~bit;
		restoreInterrupts();
	}
}

///////// DigitalInputShiftRegisterPin

DigitalInputShiftRegisterPin::DigitalInputShiftRegisterPin(DigitalInputShiftRegister *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

boolean DigitalInputShiftRegisterPin::getState() {
	return parent->getState(devicePin);
}

///////// DigitalInputShiftRegister

void DigitalInputShiftRegister::initialize(DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin) {
	this->PE_pin = PE_pin;
	this->CP_pin = CP_pin;
	this->Q7_pin = Q7_pin;

	PE_pin->setState(false);
	CP_pin->setState(false);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
}

void DigitalInputShiftRegister::update() {
	// Load data into register
	CP_pin->setState(false);
	PE_pin->setState(false);
	CP_pin->setState(true);

	// Start reading
	PE_pin->setState(true);
	uint8_t mask = 1;
	uint8_t *buf = buffer;
	for (uint8_t i = 0; i < DigitalInputShiftRegisterPinsCount; i++) {
		if (Q7_pin->getState()) {
			*buf |= mask;
		} else {
			*buf &= ~mask;
		}
		CP_pin->setState(false);
		CP_pin->setState(true);
		mask <<= 1;
		if (mask == 0) {
			buf++;
			mask = 1;
		}
	}

	PE_pin->setState(false);
	CP_pin->setState(false);
}

boolean DigitalInputShiftRegister::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= DigitalInputShiftRegisterPinsCount ? 0 :
		buffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

DigitalInputPin *DigitalInputShiftRegister::createPinHandler(const uint8_t shiftRegisterPin) {
	return new DigitalInputShiftRegisterPin(this, shiftRegisterPin);
}

///////// DigitalOutputShiftRegisterPin

DigitalOutputShiftRegisterPin::DigitalOutputShiftRegisterPin(DigitalOutputShiftRegister *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

boolean DigitalOutputShiftRegisterPin::getState() {
	return parent->getState(devicePin);
}

void DigitalOutputShiftRegisterPin::setState(const boolean value) {
	parent->setState(devicePin, value);
}

///////// DigitalOutputShiftRegister

void DigitalOutputShiftRegister::initialize(DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin) {
	this->CP_pin = CP_pin;
	this->DS_pin = DS_pin;
	modified = true;

	CP_pin->setState(false);
	DS_pin->setState(false);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
}

boolean DigitalOutputShiftRegister::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= DigitalOutputShiftRegisterPinsCount ? 0 :
		buffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

void DigitalOutputShiftRegister::setState(const uint8_t shiftRegisterPin, const boolean value) {
	if (shiftRegisterPin < DigitalOutputShiftRegisterPinsCount) {
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

void DigitalOutputShiftRegister::update() {
	if (modified) {
		uint8_t mask = 1;
		uint8_t *buf = buffer;
		for (uint8_t i = 0; i < DigitalOutputShiftRegisterPinsCount; i++) {
			CP_pin->setState(false);
			DS_pin->setState(*buf & mask);
			CP_pin->setState(true);
			mask <<= 1;
			if (mask == 0) {
				buf++;
				mask = 1;
			}
		}
		CP_pin->setState(false);
		DS_pin->setState(false);
	}
}

DigitalOutputPin *DigitalOutputShiftRegister::createPinHandler(const uint8_t shiftRegisterPin) {
	return new DigitalOutputShiftRegisterPin(this, shiftRegisterPin);
}
