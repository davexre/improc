#ifndef DIGITAL_IO_H
#define DIGITAL_IO_H

#include <Arduino.h>

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

	virtual bool getState();
};

///////// DigitalInputArduinoPin

class DigitalInputArduinoPin : public DigitalInputPin {
private:
	uint8_t bit;

	volatile uint8_t *inputRegister;
public:
	void initialize(const uint8_t arduinoPin, const bool enablePullup);

	virtual bool getState();
};

///////// DigitalOutputArduinoPin

class DigitalOutputArduinoPin : public DigitalOutputPin {
private:
	uint8_t bit;

	volatile uint8_t *outputRegister;

	bool lastState;
public:
	void initialize(const uint8_t arduinoPin, const bool initialValue = 0);

	virtual bool getState();

	virtual void setState(const bool value);
};

///////// DigitalInputShiftRegisterPin

class DigitalInputShiftRegister;
class DigitalInputShiftRegisterPin : public DigitalInputPin {
private:
	DigitalInputShiftRegister *parent;

	uint8_t devicePin;
public:
	void initialize(DigitalInputShiftRegister *parent, const uint8_t devicePin);

	virtual bool getState();
};

///////// DigitalInputShiftRegister

class DigitalInputShiftRegister {
protected:
	DigitalInputShiftRegisterPin pinHandlers[DigitalInputShiftRegisterMaxPins];
	uint8_t inputBuffer[DigitalInputShiftRegisterBufferSize];
	uint8_t inputPinsCount;
public:
	friend class DigitalInputShiftRegisterPin;

	bool getState(const uint8_t shiftRegisterPin);

	inline uint8_t getInputPinsCount() {
		return inputPinsCount;
	}

	DigitalInputPin *createPinHandler(const uint8_t shiftRegisterPin);
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
	void initialize(uint8_t inputPinsCount, DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

/**
 * Based on the datasheet for 74HC164 - No output latch - output data is shifted "on the fly".
 * The 74HC595 Has output latches
 */

class DigitalOutputShiftRegister_74HC164;
class DigitalOutputShiftRegister_74HC164_Pin : public DigitalOutputPin {
private:
	DigitalOutputShiftRegister_74HC164 *parent;

	uint8_t devicePin;
public:
	void initialize(DigitalOutputShiftRegister_74HC164 *parent, const uint8_t devicePin);

	virtual bool getState();

	virtual void setState(const bool value);
};

class DigitalOutputShiftRegister_74HC164 {
protected:
	DigitalOutputShiftRegister_74HC164_Pin pinHandlers[DigitalOutputShiftRegisterMaxPins];
	uint8_t outputBuffer[DigitalOutputShiftRegisterBufferSize];
	uint8_t outputPinsCount;
	bool modified;

	DigitalOutputPin *CP_pin;
	DigitalOutputPin *DS_pin;
public:
	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 */
	void initialize(uint8_t outputPinsCount, DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	bool getState(const uint8_t shiftRegisterPin);

	void setState(const uint8_t shiftRegisterPin, const bool value);

	inline uint8_t getOutputPinsCount() {
		return outputPinsCount;
	}

	DigitalOutputShiftRegister_74HC164_Pin *createPinHandler(const uint8_t shiftRegisterPin);
};

/**
 * Based on the datasheet for 74HC595 - Has output latch.
 */
class DigitalOutputShiftRegister_74HC595;
class DigitalOutputShiftRegister_74HC595_Pin : public DigitalOutputPin {
private:
	DigitalOutputShiftRegister_74HC595 *parent;

	uint8_t devicePin;
public:
	void initialize(DigitalOutputShiftRegister_74HC595 *parent, const uint8_t devicePin);

	virtual bool getState();

	virtual void setState(const bool value);
};

class DigitalOutputShiftRegister_74HC595 {
protected:
	DigitalOutputShiftRegister_74HC595_Pin pinHandlers[DigitalOutputShiftRegisterMaxPins];
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
	void initialize(uint8_t outputPinsCount, WriteOutputMode writeOutputMode, DigitalOutputPin *SH_pin, DigitalOutputPin *ST_pin, DigitalOutputPin *DS_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	bool getState(const uint8_t shiftRegisterPin);

	void setState(uint8_t shiftRegisterPin, const bool value);

	inline uint8_t getOutputPinsCount() {
		return outputPinsCount;
	}

	DigitalOutputShiftRegister_74HC595_Pin *createPinHandler(const uint8_t shiftRegisterPin);
};

#endif
