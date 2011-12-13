#include "DigitalIO.h"
#include "utils.h"

bool DigitalInvertingInputPin::getState() {
	return !inputPin->getState();
}

///////// DigitalInputArduinoPin

void DigitalInputArduinoPin::initialize(const uint8_t arduinoPin, const bool enablePullup) {
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

void DigitalOutputArduinoPin::initialize(const uint8_t arduinoPin, const bool initialValue) {
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

void DigitalInputShiftRegisterPin::initialize(DigitalInputShiftRegister *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

bool DigitalInputShiftRegisterPin::getState() {
	return parent->getState(devicePin);
}

///////// DigitalInputShiftRegister

bool DigitalInputShiftRegister::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= getInputPinsCount() ? 0 :
		inputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

DigitalInputPin *DigitalInputShiftRegister::createPinHandler(const uint8_t shiftRegisterPin) {
	pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
	return &pinHandlers[shiftRegisterPin];
}

///////// DigitalInputShiftRegister_74HC166

void DigitalInputShiftRegister_74HC166::initialize(uint8_t inputPinsCount, DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin) {
	if (DigitalInputShiftRegisterMaxPins > inputPinsCount)
		inputPinsCount = DigitalInputShiftRegisterMaxPins;
	this->inputPinsCount = inputPinsCount;
	this->PE_pin = PE_pin;
	this->CP_pin = CP_pin;
	this->Q7_pin = Q7_pin;

	PE_pin->setState(false);
	CP_pin->setState(false);

	for (int i = sizeof(inputBuffer) - 1; i >= 0; i--) {
		inputBuffer[i] = 0;
	}
}

void DigitalInputShiftRegister_74HC166::update() {
	// Load data into register
	CP_pin->setState(false);
	PE_pin->setState(false);
	CP_pin->setState(true);

	// Start reading
	PE_pin->setState(true);
	uint8_t mask = 1;
	uint8_t *buf = inputBuffer;
	for (uint8_t i = 0; i < inputPinsCount; i++) {
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

///////// DigitalOutputShiftRegister_74HC164

void DigitalOutputShiftRegister_74HC164_Pin::initialize(DigitalOutputShiftRegister_74HC164 *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

bool DigitalOutputShiftRegister_74HC164_Pin::getState() {
	return parent->getState(devicePin);
}

void DigitalOutputShiftRegister_74HC164_Pin::setState(const bool value) {
	parent->setState(devicePin, value);
}

void DigitalOutputShiftRegister_74HC164::initialize(uint8_t outputPinsCount,
		DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin) {
	if (DigitalInputShiftRegisterMaxPins > outputPinsCount)
		outputPinsCount = DigitalInputShiftRegisterMaxPins;
	this->outputPinsCount = outputPinsCount;
	this->CP_pin = CP_pin;
	this->DS_pin = DS_pin;
	modified = true;

	CP_pin->setState(false);
	DS_pin->setState(false);

	for (int i = sizeof(outputBuffer) - 1; i >= 0; i--) {
		outputBuffer[i] = 0;
	}
	update();
}

void DigitalOutputShiftRegister_74HC164::update() {
	if (modified) {
		modified = false;
		uint8_t mask = 1 << ((outputPinsCount - 1) & 0b0111);
		uint8_t *buf = &outputBuffer[(outputPinsCount - 1) >> 3];
		for (uint8_t i = 0; i < outputPinsCount; i++) {
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

bool DigitalOutputShiftRegister_74HC164::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= getOutputPinsCount() ? 0 :
		outputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

void DigitalOutputShiftRegister_74HC164::setState(const uint8_t shiftRegisterPin, const bool value) {
	if (shiftRegisterPin < getOutputPinsCount()) {
		uint8_t mask = 1 << (shiftRegisterPin & 0b0111);
		uint8_t *buf = &outputBuffer[shiftRegisterPin >> 3];
		if ((*buf & mask) ^ (value)) {
			modified = true;
			if (value)
				*buf |= mask;
			else
				*buf &= ~mask;
		}
	}
}

DigitalOutputShiftRegister_74HC164_Pin *DigitalOutputShiftRegister_74HC164::createPinHandler(const uint8_t shiftRegisterPin) {
	pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
	return &pinHandlers[shiftRegisterPin];
}

///////// DigitalOutputShiftRegister_74HC595

void DigitalOutputShiftRegister_74HC595_Pin::initialize(DigitalOutputShiftRegister_74HC595 *parent, const uint8_t devicePin) {
	this->parent = parent;
	this->devicePin = devicePin;
}

bool DigitalOutputShiftRegister_74HC595_Pin::getState() {
	return parent->getState(devicePin);
}

void DigitalOutputShiftRegister_74HC595_Pin::setState(const bool value) {
	parent->setState(devicePin, value);
}

void DigitalOutputShiftRegister_74HC595::initialize(uint8_t outputPinsCount, WriteOutputMode writeOutputMode,
		DigitalOutputPin *SH_pin, DigitalOutputPin *ST_pin, DigitalOutputPin *DS_pin) {
	if (DigitalOutputShiftRegisterMaxPins > outputPinsCount)
		outputPinsCount = DigitalOutputShiftRegisterMaxPins;
	this->outputPinsCount = outputPinsCount;
	this->SH_pin = SH_pin;
	this->ST_pin = ST_pin;
	this->DS_pin = DS_pin;
	this->writeOutputMode = writeOutputMode;
	modified = true;

	SH_pin->setState(false);
	ST_pin->setState(false);
	DS_pin->setState(false);

	for (int i = sizeof(outputBuffer) - 1; i >= 0; i--) {
		fakeBuffer[i] = outputBuffer[i] = 0;
	}
	update();
}

void DigitalOutputShiftRegister_74HC595::update() {
	if (modified || (writeOutputMode == DigitalOutputShiftRegister_74HC595::WriteOnEveryUpdate)) {
		ST_pin->setState(false);
		if ((writeOutputMode == BeforeWriteZeroAllOutputs) ||
			(writeOutputMode == BeforeWriteZeroOnlyModifiedOutputs)) {
			DS_pin->setState(false);	// Only 0 on the output
			uint8_t mask = 1 << ((outputPinsCount - 1) & 0b0111);
			uint8_t *buf = &outputBuffer[(outputPinsCount - 1) >> 3];
			for (uint8_t i = 0; i < outputPinsCount; i++) {
				SH_pin->setState(false);
				if (writeOutputMode == BeforeWriteZeroOnlyModifiedOutputs) {
					DS_pin->setState(*buf & mask);
					mask >>= 1;
					if (mask == 0) {
						buf--;
						mask = 0x80;
					}
				}
				SH_pin->setState(true);
			}
			ST_pin->setState(true);
			ST_pin->setState(false);
		}

		modified = false;
		uint8_t mask = 1 << ((outputPinsCount - 1) & 0b0111);
		uint8_t *buf = &outputBuffer[(outputPinsCount - 1) >> 3];
		// ST_pin->setState(false); // already done above
		for (uint8_t i = 0; i < outputPinsCount; i++) {
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

bool DigitalOutputShiftRegister_74HC595::getState(const uint8_t shiftRegisterPin) {
	return shiftRegisterPin >= getOutputPinsCount() ? 0 :
		outputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
}

void DigitalOutputShiftRegister_74HC595::setState(uint8_t shiftRegisterPin, const bool value) {
	if (shiftRegisterPin < getOutputPinsCount()) {
		uint8_t mask = 1 << (shiftRegisterPin & 0b0111);
		shiftRegisterPin >>= 3;
		uint8_t *buf = &outputBuffer[shiftRegisterPin];
		if ((*buf & mask) ^ (value)) {
			modified = true;
			fakeBuffer[shiftRegisterPin] &= ~mask;
			if (value)
				*buf |= mask;
			else
				*buf &= ~mask;
		}
	}
}

DigitalOutputShiftRegister_74HC595_Pin *DigitalOutputShiftRegister_74HC595::createPinHandler(const uint8_t shiftRegisterPin) {
	pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
	return &pinHandlers[shiftRegisterPin];
}
