#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "DigitalIO.h"

class SteppingMotor {
private:
	signed char currentState;

	byte movementMode; // 0 - goto step; 1 - move forward; 2 - move backward

	long targetStep;

	long step;

	void setState(const uint8_t state);

	boolean isMotorCoilOn;
	unsigned long motorCoilOnMicros;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;
public:

	unsigned long motorCoilTurnOffMicros;
	unsigned long motorCoilDelayBetweenStepsMicros;

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);

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

	boolean isMoving();

	inline long getStep(void) {
		return step;
	}
};

#endif
