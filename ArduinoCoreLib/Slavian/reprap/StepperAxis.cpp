#include "StepperAxis.h"

void StepperAxis::initialize(SteppingMotor *motor,
		DigitalInputPin *endPositionButtonPin) {
	motorControl.initialize(motor);
	endPositionButton.initialize(endPositionButtonPin);
	maxStep = 100;
	axisStepsPerMM = 1.0;
}

void StepperAxis::update() {
	endPositionButton.update();
	motorControl.update();

	switch (mode) {
	case StepperAxisModeDetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	case StepperAxisModeInitializeToStartingPosition:
		doInitializeToStartingPosition();
		break;
	case StepperAxisModeMoveToPosition:
		doModeMoveToPosition();
		break;

	case StepperAxisModeIdle:
	case StepperAxisModeError:
	default:
		if (motorControl.isMoving()) {
			motorControl.stop();
			mode = StepperAxisModeError;
			modeState = 0;
		}
		break;
	}
}

void StepperAxis::doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
	case 1:
	case 2:
		// These steps are absolutely the same.
		doInitializeToStartingPosition();
		break;
	case 5:
		// Rotate forward till the button gets pressed.
		motorControl.rotate(true);
		modeState = 6;
		break;
	case 6:
		if (endPositionButton.isDown()) {
			// The end position moving forward is reached.
			motorControl.stop();
			maxStep = motorControl.getStep();
			timestamp = millis();
			motorControl.rotate(false);
			modeState = 7;
		}
		break;
	case 7:
		if (millis() - timestamp > 100) {
			// Wait for 100 millis for the motor to move and release the button
			modeState = 8;
		}
		break;
	case 8:
		if (endPositionButton.isDown()) {
			// The end position moving backward is reached.
			motorControl.stop();
			maxStep -= motorControl.getStep();
			motorControl.resetStepTo(0);
			mode = StepperAxisModeIdle;
			modeState = 0;
		}
		break;
	}
}

void StepperAxis::doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		motorControl.resetStepTo(0);
		if (endPositionButton.isDown()) {
			// The motor is in some end position. Try moving forward a bit to release the button
			motorControl.gotoStep(5);
			modeState = 1;
		} else {
			modeState = 5;
		}
		break;
	case 1:
		if (!motorControl.isMoving()) {
			if (endPositionButton.isDown()) {
				// The button is still pressed. Try moving backward a bit.
				motorControl.gotoStep(0);
				modeState = 2;
			} else {
				modeState = 5;
			}
		}
		break;
	case 2:
		if (!motorControl.isMoving()) {
			if (endPositionButton.isDown()) {
				// The button is still pressed. The motor might be blocked.
				mode = StepperAxisModeError;
				modeState = 0;
			} else {
				modeState = 5;
			}
		}
		break;
	case 5:
		// Rotate backward till the button gets pressed.
		motorControl.rotate(false);
		modeState = 6;
		break;
	case 6:
		if (endPositionButton.isDown()) {
			// The end position moving backward is reached.
			motorControl.stop();
			motorControl.resetStepTo(0);
			mode = StepperAxisModeIdle;
			modeState = 0;
		}
		break;
	}
}

void StepperAxis::doModeMoveToPosition() {
	if (!motorControl.isMoving()) {
		mode = StepperAxisModeIdle;
		modeState = 0;
	} else if (endPositionButton.isDown()) {
		long atStep = motorControl.getStep();
		if ((atStep != 0) && (atStep != maxStep)) {
			motorControl.stop();
			mode = StepperAxisModeError;
			modeState = 0;
		}
	}
}

void StepperAxis::initializeToStartingPosition() {
	motorControl.stop();
	mode = StepperAxisModeInitializeToStartingPosition;
	modeState = 0;
}

void StepperAxis::determineAvailableSteps() {
	motorControl.stop();
	mode = StepperAxisModeDetermineAvailableSteps;
	modeState = 0;
}

void StepperAxis::stop(void) {
	motorControl.stop();
	mode = StepperAxisModeIdle;
	modeState = 0;
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

void StepperAxis::moveToPositionMMFast(float absolutePositionMM) {
	long targetStep = absolutePositionMM / axisStepsPerMM;
	targetStep = constrain(targetStep, 0, maxStep);
	motorControl.motorCoilDelayBetweenStepsMicros = 2000;
	motorControl.gotoStep(targetStep);
	mode = StepperAxisModeMoveToPosition;
	modeState = 0;
}

void StepperAxis::moveToPositionMM(float absolutePositionMM, unsigned long timeToMoveMillis) {
	long targetStep = absolutePositionMM / axisStepsPerMM;
	unsigned long deltaSteps = abs(targetStep - motorControl.getStep());
	motorControl.motorCoilDelayBetweenStepsMicros = (deltaSteps <= 1) ? 2000UL : (1000UL * timeToMoveMillis) / (deltaSteps - 1);
	motorControl.gotoStep(targetStep);
	mode = StepperAxisModeMoveToPosition;
	modeState = 0;
}

void StepperAxis::rotate(bool direction, float speedMMperMin) {
	unsigned long delay = 60000.0f * axisStepsPerMM / speedMMperMin;
	motorControl.motorCoilDelayBetweenStepsMicros = constrain(delay, 5UL, 20000UL);
	motorControl.rotate(direction);
}

float StepperAxis::getAbsolutePositionMM() {
	return ((float) motorControl.getStep()) * axisStepsPerMM;
}
