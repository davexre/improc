#include "StepperAxis.h"

void StepperAxis::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	motorControl.initialize(motor, startPositionButtonPin, endPositionButtonPin);
	axisStepsPerMM = 1.0;
	mode = StepperAxisModeIdle;
	modeState = 0;
}

void StepperAxis::update() {
	motorControl.update();
	switch (mode) {
	case StepperAxisModeIdle:
		break;
	case StepperAxisModeInitializeToStartingPosition:
		if (!motorControl.isMoving()) {
			motorControl.resetStepsMadeSoFar();
			mode = StepperAxisModeIdle;
			modeState = 0;
		}
		break;
	case StepperAxisModeError:
		break;
	}
}

#define StepperAxisMMToMoveBeforeInitialize 500
bool StepperAxis::isInitializeToStartingPositionNeeded() {
	if (!motorControl.isOk())
		return true;
	if ((long) (motorControl.getStepsMadeSoFar() * axisStepsPerMM) > StepperAxisMMToMoveBeforeInitialize)
		return true;
	return false;
}

void StepperAxis::initializeToStartingPosition() {
	mode = StepperAxisModeInitializeToStartingPosition;
	modeState = 0;
	motorControl.initializeToStartingPosition();
}

void StepperAxis::setMaxStep(long maxStep) {
	this->maxStep = maxStep;
}

void StepperAxis::setAxisStepsPerMM(float axisStepsPerMM) {
	if (axisStepsPerMM < 1)
		this->axisStepsPerMM = 1.0;
	else
		this->axisStepsPerMM = axisStepsPerMM;
}

void StepperAxis::determineAvailableSteps(void) {
	motorControl.determineAvailableSteps();
}

void StepperAxis::moveToPositionMMFast(float absolutePositionMM) {
	long targetStep = absolutePositionMM / axisStepsPerMM;
	targetStep = constrain(targetStep, 0, maxStep);
	motorControl.setDelayBetweenStepsMicros(2000UL);
	motorControl.gotoStep(targetStep);
}

void StepperAxis::moveToPositionMM(float absolutePositionMM, unsigned long timeToMoveMillis) {
	long targetStep = absolutePositionMM / axisStepsPerMM;
	unsigned long deltaSteps = abs(targetStep - motorControl.getStep());
	motorControl.setDelayBetweenStepsMicros(deltaSteps <= 1 ? 2000UL : (1000UL * timeToMoveMillis) / (deltaSteps - 1));
	motorControl.gotoStep(targetStep);
}

float StepperAxis::getAbsolutePositionMM() {
	return ((float) motorControl.getStep()) * axisStepsPerMM;
}
