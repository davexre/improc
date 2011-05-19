#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "TicksPerSecond.h"
#include "Button.h"

class SteppingMotor {
private:
	signed char currentState;

	byte movementMode; // 0 - goto step; 1 - move forward; 2 - move backward

	long targetStep;

	long step;

	void setState(const uint8_t state);

	boolean isMotorOn;
	long motorOnMicros;
public:
	uint8_t out11pin;
	uint8_t out12pin;
	uint8_t out21pin;
	uint8_t out22pin;

	long motorCoilTurnOffMicros;
	long motorCoilDelayBetweenStepsMicros;

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(const uint8_t out11pin, const uint8_t out12pin, const uint8_t out21pin, const uint8_t out22pin);

	/**
	 * Updates the state of the rotary encoder.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update();

	void gotoStep(const long step);

	void rotate(const boolean forward);

	void stop();

	void resetStepTo(const long step);

	inline long getStep(void) {
		return step;
	}
};

#endif
