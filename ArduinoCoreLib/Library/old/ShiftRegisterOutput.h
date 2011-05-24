#ifndef ShiftRegisterOutput_h
#define ShiftRegisterOutput_h

#include <wiring.h>

#define ShiftRegisterOutputPinsCount (8)

class ShiftRegisterOutput {
private:
	uint8_t buffer[(ShiftRegisterOutputPinsCount + 7) / 8];
	uint8_t CP_pin;
	uint8_t DS_pin;
	boolean modified;
public:

	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 *
	 * Based on the datasheet for 74HC164.
	 */
	void initialize(const uint8_t CP_pin, const uint8_t DS_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	boolean getState(const uint8_t shiftRegisterPin);

	void setState(const uint8_t shiftRegisterPin, const boolean value);
};

#endif
