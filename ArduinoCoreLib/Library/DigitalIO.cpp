#include "DigitalIO.h"
#include "utils.h"

bool DigitalInvertingInputPin::getState() {
	return !inputPin->getState();
}

///////// DigitalInputArduinoPin

DigitalInputArduinoPin::DigitalInputArduinoPin(const uint8_t arduinoPin, const bool enablePullup) {
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

bool DigitalInputArduinoPin::getState() {
	return (*inputRegister & bit);
}

///////// DigitalOutputArduinoPin

DigitalOutputArduinoPin::DigitalOutputArduinoPin(const uint8_t arduinoPin, const bool initialValue) {
	bit = digitalPinToBitMask(arduinoPin);
	uint8_t port = digitalPinToPort(arduinoPin);
	outputRegister = portOutputRegister(port);
	pinMode(arduinoPin, OUTPUT);
	setState(initialValue);
}

bool DigitalOutputArduinoPin::getState() {
	return lastState;
}

void DigitalOutputArduinoPin::setState(const bool value) {
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

bool DigitalInputShiftRegisterPin::getState() {
	return parent->getState(devicePin);
}

///////// DigitalInputShiftRegister

bool DigitalInputShiftRegister::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= DigitalInputShiftRegisterPinsCount ? 0 :
		buffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

DigitalInputPin *DigitalInputShiftRegister::createPinHandler(const uint8_t shiftRegisterPin) {
	return new DigitalInputShiftRegisterPin(this, shiftRegisterPin);
}

///////// DigitalInputShiftRegister_74HC166

void DigitalInputShiftRegister_74HC166::initialize(DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin) {
	this->PE_pin = PE_pin;
	this->CP_pin = CP_pin;
	this->Q7_pin = Q7_pin;

	PE_pin->setState(false);
	CP_pin->setState(false);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
}

void DigitalInputShiftRegister_74HC166::update() {
	// Load data into register
	CP_pin->setState(false);
	PE_pin->setState(false);
	CP_pin->setState(true);

	// Start reading
	PE_pin->setState(true);
	uint8_t mask = 1 << ((DigitalInputShiftRegisterPinsCount - 1) & 0b0111);
	uint8_t *buf = &buffer[(DigitalInputShiftRegisterPinsCount - 1) >> 3];
	for (uint8_t i = 0; i < DigitalInputShiftRegisterPinsCount; i++) {
		if (Q7_pin->getState()) {
			*buf |= mask;
		} else {
			*buf &= ~mask;
		}
		CP_pin->setState(false);
		CP_pin->setState(true);
		mask >>= 1;
		if (mask == 0) {
			buf--;
			mask = 0x80;
		}
	}

	PE_pin->setState(false);
	CP_pin->setState(false);
}

///////// DigitalOutputShiftRegisterPin

DigitalOutputShiftRegisterPin::DigitalOutputShiftRegisterPin(DigitalOutputShiftRegister *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

bool DigitalOutputShiftRegisterPin::getState() {
	return parent->getState(devicePin);
}

void DigitalOutputShiftRegisterPin::setState(const bool value) {
	parent->setState(devicePin, value);
}

///////// DigitalOutputShiftRegister

bool DigitalOutputShiftRegister::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= DigitalOutputShiftRegisterPinsCount ? 0 :
		buffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

void DigitalOutputShiftRegister::setState(const uint8_t shiftRegisterPin, const bool value) {
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

DigitalOutputPin *DigitalOutputShiftRegister::createPinHandler(const uint8_t shiftRegisterPin) {
	return new DigitalOutputShiftRegisterPin(this, shiftRegisterPin);
}

///////// DigitalOutputShiftRegister_74HC164

void DigitalOutputShiftRegister_74HC164::initialize(DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin) {
	this->CP_pin = CP_pin;
	this->DS_pin = DS_pin;
	modified = true;

	CP_pin->setState(false);
	DS_pin->setState(false);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
	update();
}

void DigitalOutputShiftRegister_74HC164::update() {
	if (modified) {
		modified = false;
		uint8_t mask = 1 << ((DigitalOutputShiftRegisterPinsCount - 1) & 0b0111);
		uint8_t *buf = &buffer[(DigitalOutputShiftRegisterPinsCount - 1) >> 3];
		for (uint8_t i = 0; i < DigitalOutputShiftRegisterPinsCount; i++) {
			CP_pin->setState(false);
			DS_pin->setState(*buf & mask);
			CP_pin->setState(true);
			mask >>= 1;
			if (mask == 0) {
				buf--;
				mask = 0x80;
			}
		}
		CP_pin->setState(false);
		DS_pin->setState(false);
	}
}

///////// DigitalOutputShiftRegister_74HC595

void DigitalOutputShiftRegister_74HC595::initialize(DigitalOutputPin *SH_pin, DigitalOutputPin *ST_pin, DigitalOutputPin *DS_pin) {
	this->SH_pin = SH_pin;
	this->ST_pin = ST_pin;
	this->DS_pin = DS_pin;
	modified = true;

	SH_pin->setState(false);
	ST_pin->setState(false);
	DS_pin->setState(false);

	for (int i = sizeof(buffer) - 1; i >= 0; i--) {
		buffer[i] = 0;
	}
	update();
}

void DigitalOutputShiftRegister_74HC595::update() {
	if (modified) {
		modified = false;
		uint8_t mask = 1 << ((DigitalOutputShiftRegisterPinsCount - 1) & 0b0111);
		uint8_t *buf = &buffer[(DigitalOutputShiftRegisterPinsCount - 1) >> 3];
		ST_pin->setState(false);
		for (uint8_t i = 0; i < DigitalOutputShiftRegisterPinsCount; i++) {
			SH_pin->setState(false);
			DS_pin->setState(*buf & mask);
			SH_pin->setState(true);
			mask >>= 1;
			if (mask == 0) {
				buf--;
				mask = 0x80;
			}
		}
		SH_pin->setState(false);
		DS_pin->setState(false);
		ST_pin->setState(true);
		ST_pin->setState(false);
	}
}

