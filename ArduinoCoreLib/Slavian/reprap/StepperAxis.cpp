#include "StepperAxis.h"

void StepperAxis::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	motorControl.initialize(motor, startPositionButtonPin, endPositionButtonPin);
	axisSteps = -1;
	axisLengthInMicroM = 1;
	axisHomePositionMicroM = 0;
	delayBetweenStepsAtMaxSpeedMicros = 2000;
	useStartPositionToInitialize = true;
	mode = StepperAxis::Idle;
	modeState = 0;
}

void StepperAxis::update() {
	motorControl.update();
	switch (mode) {
	case Idle:
		break;
	case InitializePosition:
		if (!motorControl.isMoving()) {
			motorControl.resetStepsMadeSoFar();
			mode = StepperAxis::Idle;
			modeState = 0;
		}
		break;
	case Error:
		break;
	}
}

bool StepperAxis::isInitializePositionNeeded() {
	if (!motorControl.isOk())
		return true;
	if ((long) (motorControl.getStepsMadeSoFar() * axisLengthInMicroM / axisSteps) > StepperAxisMicroMToMoveBeforeInitialize)
		return true;
	return false;
}

void StepperAxis::initializePosition() {
	mode = StepperAxis::InitializePosition;
	modeState = 0;
	motorControl.setDelayBetweenStepsMicros(delayBetweenStepsAtMaxSpeedMicros);
	if (useStartPositionToInitialize)
		motorControl.initializeToStartPosition();
	else
		motorControl.initializeToEndPosition();
}

void StepperAxis::setAxisSteps(long axisSteps) {
	this->axisSteps = axisSteps;
}

void StepperAxis::setAxisLengthInMicroM(long axisLengthInMicroM) {
	this->axisLengthInMicroM = max(1, axisLengthInMicroM);
}

void StepperAxis::determineAvailableSteps(void) {
	motorControl.determineAvailableSteps();
}

void StepperAxis::moveToPositionMicroMFast(long absolutePositionMicroM) {
	long targetStep = absolutePositionMicroM * axisSteps / axisLengthInMicroM;
	targetStep = constrain(targetStep, 0, axisSteps);
	motorControl.setDelayBetweenStepsMicros(delayBetweenStepsAtMaxSpeedMicros);
	motorControl.gotoStep(targetStep);
}

void StepperAxis::moveToPositionMicroM(long absolutePositionMicroM, unsigned long timeToMoveMicros) {
	long targetStep = absolutePositionMicroM * axisSteps / axisLengthInMicroM;
	unsigned long delta = abs(targetStep - motorControl.getStep());
	if (delta <= 1) {
		delta = delayBetweenStepsAtMaxSpeedMicros;
	} else {
		delta = timeToMoveMicros / (delta - 1);
		if (delta < delayBetweenStepsAtMaxSpeedMicros)
			delta = delayBetweenStepsAtMaxSpeedMicros;
	}
	motorControl.setDelayBetweenStepsMicros(delta);
	motorControl.gotoStep(targetStep);
}

long StepperAxis::getAbsolutePositionMicroM() {
	return motorControl.getStep() * axisLengthInMicroM / axisSteps;
}
