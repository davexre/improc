#ifndef SteppingMotorControl2_h
#define SteppingMotorControl2_h

#include "SteppingMotor.h"
#include "Button.h"

#define SteppingMotorControlIdle 0
#define SteppingMotorControlError 1
#define SteppingMotorControlDetermineAvailableSteps 10
#define SteppingMotorControlInitializeToStartingPosition 11

class SteppingMotorControlWithButtons : SteppingMotorControl {
private:
	Button startPositionButton;

	Button endPositionButton;

	uint8_t mode;

	uint8_t modeState;

	long maxStep;

	void doInitializeToStartingPosition();

	void doDetermineAvailableSteps();

public:
	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor,
			DigitalInputPin *startPositionButtonPin,
			DigitalInputPin *endPositionButtonPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	void gotoStep(const long step);

	void rotate(const bool forward);

	void stop();

	inline bool isMoving() {
		return ((mode != SteppingMotorControlIdle) && (mode != SteppingMotorControlError)) ||
			(SteppingMotorControl::isMoving());
	}

	inline long getStep(void) {
		return SteppingMotorControl::getStep();
	}

	inline long getStepsMadeSoFar(void) {
		return SteppingMotorControl::getStepsMadeSoFar();
	}

	inline void resetStepsMadeSoFar(void) {
		SteppingMotorControl::resetStepsMadeSoFar();
	}
};

#endif
