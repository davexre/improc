#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "DigitalIO.h"

class SteppingMotor {
public:
	virtual void step(const bool moveForward) = 0;
	virtual void stop() = 0;
};

class SteppingMotor_BA6845FS : public SteppingMotor {
	signed char currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
	unsigned long motorCoilOnMicros;
	bool isMotorCoilOn;
public:
	unsigned long motorCoilTurnOffMicros;

	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

class SteppingMotor_MosfetHBridge : public SteppingMotor {
	signed char currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
	unsigned long motorCoilOnMicros;
	uint8_t mode;
public:
	unsigned long motorCoilTurnOffMicros;

	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

class SteppingMotorControl {
private:
	byte movementMode; // 0 - goto step; 1 - move forward; 2 - move backward

	long targetStep;

	long step;

	unsigned long motorCoilOnMicros;

	SteppingMotor *motor;
public:
	unsigned long motorCoilDelayBetweenStepsMicros;

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	void gotoStep(const long step);

	void rotate(const bool forward);

	void stop();

	void resetStepTo(const long step);

	bool isMoving();

	inline long getStep(void) {
		return step;
	}
};

#endif
