#ifndef ShiftRegisterInput_h
#define ShiftRegisterInput_h

#include <wiring.h>

#define ShiftRegisterInputPinsCount 8

class ShiftRegisterInput {
private:
	uint8_t buffer[(ShiftRegisterInputPinsCount + 7) / 8];
	uint8_t PE_pin;
	uint8_t CP_pin;
	uint8_t Q7_pin;
public:

	/**
	 * Initializes the class. Should be invoked from the setup() method.
	 *
	 * Based on the datasheet for 74F166.
	 */
	void initialize(const uint8_t PE_pin, const uint8_t CP_pin, const uint8_t Q7_pin);

	/**
	 * Updates the state of the shift register.
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	boolean getState(const uint8_t shifRegisterPin);
};

#endif
