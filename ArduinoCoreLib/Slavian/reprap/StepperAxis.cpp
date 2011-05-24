#include "StepperAxis.h"

void StepperAxis::initialize(
		DigitalInputPin  *endPositionButtonPin,
		DigitalOutputPin *stepMotor11pin,
		DigitalOutputPin *stepMotor12pin,
		DigitalOutputPin *stepMotor21pin,
		DigitalOutputPin *stepMotor22pin) {
	endPositionButton.initialize(endPositionButtonPin);
	motor.initialize(stepMotor11pin, stepMotor12pin, stepMotor21pin, stepMotor22pin);
}

void StepperAxis::update() {
	endPositionButton.update();
	motor.update();

	switch (mode) {
	case StepperAxisModeDetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	case StepperAxisModeInitializeToStartinPosition:
		doInitializeToStartingPosition();
		break;
	case StepperAxisModeMoveToPositionFast:
		doModeMoveToPositionFast();
		break;

	case StepperAxisModeIdle:
	case StepperAxisModeError:
	default:
		if (motor.isMoving()) {
			motor.stop();
			mode = StepperAxisModeError;
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
		motor.rotate(true);
		modeState = 6;
		break;
	case 6:
		if (endPositionButton.isDown()) {
			// The end position moving forward is reached.
			motor.stop();
			maxStep = motor.getStep();
			motor.rotate(false);
			modeState = 7;
		}
		break;
	case 7:
		if (!endPositionButton.isDown()) {
			// The end position moving backward is reached.
			motor.stop();
			maxStep -= motor.getStep();
			motor.resetStepTo(0);
			mode = StepperAxisModeIdle;
			modeState = 0;
		}
		break;
	}
}

void StepperAxis::doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		motor.resetStepTo(0);
		if (endPositionButton.isDown()) {
			// The motor is in some end position. Try moving forward a bit to release the button
			motor.gotoStep(5);
			modeState = 1;
		} else {
			modeState = 5;
		}
		break;
	case 1:
		if (!motor.isMoving()) {
			if (endPositionButton.isDown()) {
				// The button is still pressed. Try moving backward a bit.
				motor.gotoStep(0);
				modeState = 2;
			} else {
				modeState = 5;
			}
		}
		break;
	case 2:
		if (!motor.isMoving()) {
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
		motor.rotate(false);
		modeState = 6;
		break;
	case 6:
		if (endPositionButton.isDown()) {
			// The end position moving backward is reached.
			motor.stop();
			motor.resetStepTo(0);
			mode = StepperAxisModeIdle;
			modeState = 0;
		}
		break;
	}
}

void StepperAxis::doModeMoveToPositionFast() {
	if (!motor.isMoving()) {
		mode = StepperAxisModeIdle;
	} else if (endPositionButton.isDown()) {
		long atStep = motor.getStep();
		if ((atStep != 0) && (atStep != maxStep)) {
			motor.stop();
			mode = StepperAxisModeError;
			modeState = 0;
		}
	}
}

void StepperAxis::moveToPositionFast(long position) {
	position = constrain(position, 0, maxStep);
	motor.gotoStep(position);
	mode = StepperAxisModeMoveToPositionFast;
	modeState = 0;
}

void StepperAxis::initializeToStartingPosition() {
	motor.stop();
	mode = StepperAxisModeInitializeToStartinPosition;
	modeState = 0;
}

void StepperAxis::determineAvailableSteps() {
	motor.stop();
	mode = StepperAxisModeDetermineAvailableSteps;
	modeState = 0;
}
