#ifndef DIGITAL_IO_H
#define DIGITAL_IO_H

#include <wiring.h>
#include <pins_arduino.h>

#define DigitalInputShiftRegisterPinsCount (8*2+1)

#define DigitalOutputShiftRegisterPinsCount (8*2+1)


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
	inline DigitalInvertingInputPin() {
		initialize(NULL);
	};

	inline DigitalInvertingInputPin(DigitalInputPin *inputPin) {
		initialize(inputPin);
	}

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
	DigitalInputArduinoPin(const uint8_t arduinoPin, const bool enablePullup);

	virtual bool getState();
};

///////// DigitalOutputArduinoPin

class DigitalOutputArduinoPin : public DigitalOutputPin {
private:
	uint8_t bit;

	volatile uint8_t *outputRegister;

	bool lastState;
public:
	DigitalOutputArduinoPin(const uint8_t arduinoPin, const bool initialValue = 0);

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
	DigitalInputShiftRegisterPin(DigitalInputShiftRegister *parent, const uint8_t devicePin);

	virtual bool getState();
};

///////// DigitalInputShiftRegister

class DigitalInputShiftRegister {
protected:
	uint8_t buffer[(DigitalInputShiftRegisterPinsCount + 7) / 8];
public:
	friend class DigitalInputShiftRegisterPin;

	bool getState(const uint8_t shiftRegisterPin);

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
	void initialize(DigitalOutputPin *PE_pin, DigitalOutputPin *CP_pin, DigitalInputPin *Q7_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

///////// DigitalOutputShiftRegisterPin

class DigitalOutputShiftRegister;
class DigitalOutputShiftRegisterPin : public DigitalOutputPin {
private:
	DigitalOutputShiftRegister *parent;

	uint8_t devicePin;
public:
	DigitalOutputShiftRegisterPin(DigitalOutputShiftRegister *parent, const uint8_t devicePin);

	virtual bool getState();

	virtual void setState(const bool value);
};

///////// DigitalOutputShiftRegister

class DigitalOutputShiftRegister {
protected:
	uint8_t buffer[(DigitalOutputShiftRegisterPinsCount + 7) / 8];
	bool modified;
public:
	bool getState(const uint8_t shiftRegisterPin);

	void setState(const uint8_t shiftRegisterPin, const bool value);

	DigitalOutputPin *createPinHandler(const uint8_t shiftRegisterPin);
};

/**
 * Based on the datasheet for 74HC164 - No output latch - output data is shifted "on the fly".
 * The 74HC595 Has output latches
 */
class DigitalOutputShiftRegister_74HC164 : public DigitalOutputShiftRegister {
protected:
	DigitalOutputPin *CP_pin;
	DigitalOutputPin *DS_pin;
public:
	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 */
	void initialize(DigitalOutputPin *CP_pin, DigitalOutputPin *DS_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

/**
 * Based on the datasheet for 74HC595 - Has output latch.
 */
class DigitalOutputShiftRegister_74HC595 : public DigitalOutputShiftRegister {
protected:
	DigitalOutputPin *SH_pin;
	DigitalOutputPin *ST_pin;
	DigitalOutputPin *DS_pin;
public:
	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 */
	void initialize(DigitalOutputPin *SH_pin, DigitalOutputPin *ST_pin, DigitalOutputPin *DS_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

#endif
