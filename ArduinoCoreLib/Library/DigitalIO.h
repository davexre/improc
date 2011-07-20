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

/**
 * Based on the datasheet for 74F166.
 */
class DigitalInputShiftRegister {
private:
	uint8_t buffer[(DigitalInputShiftRegisterPinsCount + 7) / 8];
	DigitalOutputPin *PE_pin;
	DigitalOutputPin *CP_pin;
	DigitalInputPin *Q7_pin;
public:
	friend class DigitalInputShiftRegisterPin;

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

	bool getState(const uint8_t shiftRegisterPin);

	DigitalInputPin *createPinHandler(const uint8_t shiftRegisterPin);
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

/**
 * Based on the datasheet for 74HC164 - No output latch - output data is shifted "on the fly".
 * The 74HC595 Has output latches
 */
class DigitalOutputShiftRegister {
private:
	uint8_t buffer[(DigitalOutputShiftRegisterPinsCount + 7) / 8];
	DigitalOutputPin *CP_pin;
	DigitalOutputPin *DS_pin;
	bool modified;
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

	bool getState(const uint8_t shiftRegisterPin);

	void setState(const uint8_t shiftRegisterPin, const bool value);

	DigitalOutputPin *createPinHandler(const uint8_t shiftRegisterPin);
};

#endif
