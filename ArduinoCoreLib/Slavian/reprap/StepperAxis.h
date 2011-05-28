#ifndef StepperAxis_h
#define StepperAxis_h

#include "DigitalIO.h"
#include "Button.h"
#include "SteppingMotor.h"
#include "utils.h"

#define StepperAxisModeIdle 0
#define StepperAxisModeError 1
#define StepperAxisModeDetermineAvailableSteps 10
#define StepperAxisModeInitializeToStartinPosition 11
#define StepperAxisModeMoveToPosition 12

class StepperAxis {
private:

	byte mode;
	byte modeState;

	long maxStep;
	float axisStepsPerMM;

	void doDetermineAvailableSteps();
	void doInitializeToStartingPosition();
	void doModeMoveToPosition();
public:
	Button endPositionButton;
	SteppingMotor motor;

	/**
	 * Initializes the class.
	 */
	void initialize(
			DigitalInputPin *endPositionButtonPin,
			DigitalOutputPin *stepMotor11pin,
			DigitalOutputPin *stepMotor12pin,
			DigitalOutputPin *stepMotor21pin,
			DigitalOutputPin *stepMotor22pin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);

	inline byte getMode() { return mode; }

	void setMaxStep(long maxStep);
	inline long getMaxStep() { return maxStep; }

	void setAxisStepsPerMM(float axisStepsPer);
	inline float getAxisStepsPerMM() {
		return axisStepsPerMM;
	}

	void determineAvailableSteps(void);
	void initializeToStartingPosition(void);
	void moveToPositionFast(long position);
	void moveToPositionMM(float absolutePositionMM, unsigned long timeToMoveMillis);
	float getAbsolutePositionMM();

	void stop(void);
};

#endif
