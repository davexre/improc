#ifndef DIGITAL_IO_H
#define DIGITAL_IO_H

#include <Arduino.h>
#include "utils.h"

static const uint8_t DigitalInputShiftRegisterMaxPins = 9;
static const uint8_t DigitalOutputShiftRegisterMaxPins = 17;

static const uint8_t DigitalInputShiftRegisterBufferSize = (DigitalInputShiftRegisterMaxPins + 8) / 8;
static const uint8_t DigitalOutputShiftRegisterBufferSize = (DigitalOutputShiftRegisterMaxPins + 8) / 8;

class DigitalInputPin {
public:
	virtual bool getState() = 0;
};

class DigitalOutputPin { //: public DigitalInputPin {
public:
	virtual bool getState() = 0;

	virtual void setState(const bool value) = 0;
};

class DigitalInvertingInputPin {
private:
	DigitalInputPin *inputPin;
public:
	inline void initialize(DigitalInputPin *inputPin) {
		this->inputPin = inputPin;
	}

	virtual bool getState() {
		return !inputPin->getState();
	}
};

///////// DigitalInputArduinoPin

class DigitalInputArduinoPin : public DigitalInputPin {
private:
	uint8_t bit;

	volatile uint8_t *inputRegister;
public:
	void initialize(const uint8_t arduinoPin, const bool enablePullup) {
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

	virtual bool getState() {
		return (*inputRegister & bit);
	}
};

///////// DigitalOutputArduinoPin

class DigitalOutputArduinoPin : public DigitalOutputPin {
private:
	uint8_t bit;

	volatile uint8_t *outputRegister;

	bool lastState;
public:
	void initialize(const uint8_t arduinoPin, const bool initialValue = 0) {
		bit = digitalPinToBitMask(arduinoPin);
		uint8_t port = digitalPinToPort(arduinoPin);
		outputRegister = portOutputRegister(port);
		pinMode(arduinoPin, OUTPUT);
		setState(initialValue);
	}

	virtual bool getState() {
		return lastState;
	}

	virtual void setState(const bool value) {
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
};

///////// DigitalInputShiftRegisterPin

template <class InputShiftRegister>
class DigitalInputShiftRegisterPin : public DigitalInputPin {
private:
	InputShiftRegister *parent;

	uint8_t devicePin;
public:
	void initialize(InputShiftRegister *parent, const uint8_t devicePin) {
		this->parent = parent;
		this->devicePin = devicePin;
	}

	virtual bool getState() {
		return parent->getState(devicePin);
	}
};

///////// DigitalInputShiftRegister

class DigitalInputShiftRegister {
protected:
	DigitalInputShiftRegisterPin<DigitalInputShiftRegister> pinHandlers[DigitalInputShiftRegisterMaxPins];
	uint8_t inputBuffer[DigitalInputShiftRegisterBufferSize];
	uint8_t inputPinsCount;
public:
	bool getState(const uint8_t shiftRegisterPin) {
		return shiftRegisterPin >= getInputPinsCount() ? 0 :
			inputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
	}

	inline uint8_t getInputPinsCount() {
		return inputPinsCount;
	}

	DigitalInputPin *createPinHandler(const uint8_t shiftRegisterPin) {
		pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
		return &pinHandlers[shiftRegisterPin];
	}
};

/**
 * Based on the datasheet for 74F166.
 *
 * Pins on the chip D0-D7 are numbered in REVERSE order by the software, i.e.
 * port D7->DigitalInputPin(0), D6->DigitalInputPin(1),
 * D0->DigitalInputPin(7), DS->DigitalInputPin(8).
 * This reverse numbering allows for adding a new chip to the chip chain and not
 * re-numbering all the other pins.
 *
 * Example (2 chips):
 * DigitalInputPin(0) -> Chip 1, pin D7
 * DigitalInputPin(1) -> Chip 1, pin D6
 * DigitalInputPin(2) -> Chip 1, pin D5
 * DigitalInputPin(3) -> Chip 1, pin D4
 * DigitalInputPin(4) -> Chip 1, pin D3
 * DigitalInputPin(5) -> Chip 1, pin D2
 * DigitalInputPin(6) -> Chip 1, pin D1
 * DigitalInputPin(7) -> Chip 1, pin D0
 * DigitalInputPin(8) -> Chip 2, pin D7
 * DigitalInputPin(9) -> Chip 2, pin D6
 * DigitalInputPin(10)-> Chip 2, pin D5
 * DigitalInputPin(11)-> Chip 2, pin D4
 * DigitalInputPin(12)-> Chip 2, pin D3
 * DigitalInputPin(13)-> Chip 2, pin D2
 * DigitalInputPin(14)-> Chip 2, pin D1
 * DigitalInputPin(15)-> Chip 2, pin D0
 * DigitalInputPin(16)-> Chip 2, pin DS
 */
class DigitalInputShiftRegister_74HC166 : public DigitalInputShiftRegister {
protected:
	DigitalOutputPin *PE_pin;
	DigitalOutputPin *CP_pin;
	DigitalInputPin *Q7_pin;
public:
	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 * The Q7_pin should be with a disabled internal pull-up resistor.
	 */
	void initialize(uint8_t inputPinsCount, DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin) {
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

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update() {
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
};

/**
 * Based on the datasheet for 74HC164 - No output latch - output data is shifted "on the fly".
 * The 74HC595 Has output latches
 */
template <class OutputShiftRegister>
class DigitalOutputShiftRegisterPin : public DigitalOutputPin {
private:
	OutputShiftRegister *parent;

	uint8_t devicePin;
public:
	void initialize(OutputShiftRegister *parent, const uint8_t devicePin) {
		this->parent = parent;
		this->devicePin = devicePin;
	}

	virtual bool getState() {
		return parent->getState(devicePin);
	}

	virtual void setState(const bool value) {
		parent->setState(devicePin, value);
	}
};

class DigitalOutputShiftRegister_74HC164 {
protected:
	DigitalOutputShiftRegisterPin<DigitalOutputShiftRegister_74HC164> pinHandlers[DigitalOutputShiftRegisterMaxPins];
	uint8_t outputBuffer[DigitalOutputShiftRegisterBufferSize];
	uint8_t outputPinsCount;
	bool modified;

	DigitalOutputPin *CP_pin;
	DigitalOutputPin *DS_pin;
public:
	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 */
	void initialize(uint8_t outputPinsCount, DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin) {
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

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update() {
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

	bool getState(const uint8_t shiftRegisterPin) {
		return shiftRegisterPin >= getOutputPinsCount() ? 0 :
			outputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
	}

	void setState(const uint8_t shiftRegisterPin, const bool value) {
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

	inline uint8_t getOutputPinsCount() {
		return outputPinsCount;
	}

	DigitalOutputPin *createPinHandler(const uint8_t shiftRegisterPin) {
		pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
		return &pinHandlers[shiftRegisterPin];
	}
};

/**
 * Based on the datasheet for 74HC595 - Has output latch.
 */
class DigitalOutputShiftRegister_74HC595 {
protected:
	DigitalOutputShiftRegisterPin<DigitalOutputShiftRegister_74HC595> pinHandlers[DigitalOutputShiftRegisterMaxPins];
	uint8_t writeOutputMode;
	uint8_t outputBuffer[DigitalOutputShiftRegisterBufferSize];
	uint8_t fakeBuffer[DigitalOutputShiftRegisterBufferSize];
	uint8_t outputPinsCount;
	bool modified;

	DigitalOutputPin *SH_pin;
	DigitalOutputPin *ST_pin;
	DigitalOutputPin *DS_pin;
public:
	enum WriteOutputMode {
		/**
		 * The content of outputBuffer is sent to the shift register(s) on every invokation of the update() method.
		 */
		WriteOnEveryUpdate = 0,

		/**
		 * The content of outputBuffer is sent to the shift register(s) only if
		 * at least one pin has changed state since the last invokation of the
		 * update() method.
		 */
		WriteOnlyIfModified = 1,

		/**
		 * Similar to WriteOnlyIfModified the content of the outputBuffer is sent
		 * to the shift register(s) only if at least one pin has changed state since
		 * the last invokation of the update() method.
		 * Before the actual content of the outputBuffer is sent a "fake" outputBuffer
		 * containing only zeros is sent.
		 */
		BeforeWriteZeroAllOutputs = 2,

		/**
		 * Similar to BeforeWriteZeroAllOutputs but only the content of the modified pins
		 * is zeroed before writing the actual content.
		 */
		BeforeWriteZeroOnlyModifiedOutputs = 3
	};

	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 * zeroOutputBeforeWrite	This is usefull when H-Bridges are connected to the outputs. All
	 * 			outputs will be set to zero before the new values are written, i.e. this is the
	 * 			timeout necessary for the MOSFETs in a H-Bridge to close and avoid shortcircuit.
	 */
	void initialize(uint8_t outputPinsCount, WriteOutputMode writeOutputMode, DigitalOutputPin *SH_pin, DigitalOutputPin *ST_pin, DigitalOutputPin *DS_pin) {
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

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update() {
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

	bool getState(const uint8_t shiftRegisterPin) {
		return shiftRegisterPin >= getOutputPinsCount() ? 0 :
			outputBuffer[shiftRegisterPin >> 3] & (1 << (shiftRegisterPin & 0b0111));
	}

	void setState(uint8_t shiftRegisterPin, const bool value) {
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

	inline uint8_t getOutputPinsCount() {
		return outputPinsCount;
	}

	DigitalOutputPin *createPinHandler(const uint8_t shiftRegisterPin) {
		pinHandlers[shiftRegisterPin].initialize(this, shiftRegisterPin);
		return &pinHandlers[shiftRegisterPin];
	}
};

#endif
