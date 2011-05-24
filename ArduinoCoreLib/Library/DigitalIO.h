#ifndef DIGITAL_IO_H
#define DIGITAL_IO_H

#include <wiring.h>
#include <pins_arduino.h>

#define DigitalInputShiftRegisterPinsCount (8*2+1)

#define DigitalOutputShiftRegisterPinsCount (8*2+1)


class DigitalInputPin {
public:
	virtual boolean getState() = 0;
};


class DigitalOutputPin : public DigitalInputPin {
public:
	virtual boolean getState() = 0;

	virtual void setState(const boolean value) = 0;
};

///////// DigitalInputArduinoPin

class DigitalInputArduinoPin : public DigitalInputPin {
private:
	uint8_t bit;

	volatile uint8_t *inputRegister;
public:
	DigitalInputArduinoPin(const uint8_t arduinoPin, const boolean enablePullup = false);

	virtual boolean getState();
};

///////// DigitalOutputArduinoPin

class DigitalOutputArduinoPin : public DigitalOutputPin {
private:
	uint8_t bit;

	volatile uint8_t *outputRegister;

	boolean lastState;
public:
	DigitalOutputArduinoPin(const uint8_t arduinoPin, const boolean initialValue = 0);

	virtual boolean getState();

	virtual void setState(const boolean value);
};

///////// DigitalInputShiftRegisterPin

class DigitalInputShiftRegister;
class DigitalInputShiftRegisterPin : public DigitalInputPin {
private:
	DigitalInputShiftRegister *parent;

	uint8_t devicePin;
public:
	DigitalInputShiftRegisterPin(DigitalInputShiftRegister *parent, const uint8_t devicePin);

	virtual boolean getState();
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

	boolean getState(const uint8_t shiftRegisterPin);

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

	virtual boolean getState();

	virtual void setState(const boolean value);
};

///////// DigitalOutputShiftRegister

/**
 * Based on the datasheet for 74HC164.
 */
class DigitalOutputShiftRegister {
private:
	uint8_t buffer[(DigitalOutputShiftRegisterPinsCount + 7) / 8];
	DigitalOutputPin *CP_pin;
	DigitalOutputPin *DS_pin;
	boolean modified;
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

	boolean getState(const uint8_t shiftRegisterPin);

	void setState(const uint8_t shiftRegisterPin, const boolean value);

	DigitalOutputPin *createPinHandler(const uint8_t shiftRegisterPin);
};

#endif
