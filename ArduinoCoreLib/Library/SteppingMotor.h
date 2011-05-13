#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "TicksPerSecond.h"
#include "Button.h"

class SteppingMotor {
private:
	signed int currentState;

	void setState(const uint8_t *state);
public:
	uint8_t out11pin;
	uint8_t out12pin;
	uint8_t out21pin;
	uint8_t out22pin;

	int step;

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(uint8_t out11pin, uint8_t out12pin, uint8_t out21pin, uint8_t out22pin);

	/**
	 * Updates the state of the rotary encoder.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update();

	void previousStep();
	void nextStep();
	void stop();
};

#endif
