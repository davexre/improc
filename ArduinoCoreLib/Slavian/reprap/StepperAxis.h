#ifndef StepperAxis_h
#define StepperAxis_h

#include "DigitalIO.h"
#include "Button.h"
#include "SteppingMotor.h"
#include "utils.h"

#define StepperAxisModeIdle 0
#define StepperAxisModeError 1
#define StepperAxisModeInitializeToStartingPosition 10

class StepperAxis {
private:
	SteppingMotorControlWithButtons motorControl;
	unsigned long timestamp;
	long maxStep;
	float axisStepsPerMM;

	void doDetermineAvailableSteps();
	void doInitializeToStartingPosition();
	void doModeMoveToPosition();

	uint8_t mode;
	uint8_t modeState;
public:
	/**
	 * Initializes the class.
	 */
	void initialize(SteppingMotor *motor,
			DigitalInputPin *startPositionButtonPin,
			DigitalInputPin *endPositionButtonPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);

	void setMaxStep(long maxStep);
	inline long getMaxStep() { return maxStep; }

	void setAxisStepsPerMM(float axisStepsPer);
	inline float getAxisStepsPerMM() {
		return axisStepsPerMM;
	}

	void determineAvailableSteps(void);

	void moveToPositionMMFast(float absolutePositionMM);
	void moveToPositionMM(float absolutePositionMM, unsigned long timeToMoveMillis);
	float getAbsolutePositionMM();
	inline long getStepPosition() {
		motorControl.getStep();
	}

	void stop(void) {
		motorControl.stop();
	}

	inline bool isMoving() {
		motorControl.isMoving();
	}

	inline bool isOk() {
		return motorControl.isOk();
	}

	bool isInitializeToStartingPositionNeeded();
	void initializeToStartingPosition();
};

#endif
