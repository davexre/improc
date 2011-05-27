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
#define StepperAxisModeMoveToPositionFast 12

class StepperAxis {
private:

	byte mode;
	byte modeState;

	long maxStep;

	void doDetermineAvailableSteps();
	void doInitializeToStartingPosition();
	void doModeMoveToPositionFast();
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
	inline long getMaxStep() { return maxStep; }

	void determineAvailableSteps(void);
	void initializeToStartingPosition(void);
	void moveToPositionFast(long position);

	void stop(void);
};

#endif
