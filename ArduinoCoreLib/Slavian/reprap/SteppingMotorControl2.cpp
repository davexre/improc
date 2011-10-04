#include "Arduino.h"
#include "utils.h"
#include "SteppingMotorControl2.h"

void SteppingMotorControlWithButtons::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	SteppingMotorControl::initialize(motor);
	startPositionButton = startPositionButtonPin;
	endPositionButton = endPositionButtonPin;
	mode = SteppingMotorControlIdle;
	modeState = 0;
	minStep = 0;
	maxStep = 0;
}

void SteppingMotorControlWithButtons::gotoStep(const long step) {
	SteppingMotorControl::gotoStep(step);
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::rotate(const bool forward) {
	SteppingMotorControl::rotate(forward);
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::stop() {
	SteppingMotorControl::stop();
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		// Start moving backward to begining
		SteppingMotorControl::resetStepTo(0);
		if (startPositionButton->getState()) {
			SteppingMotorControl::stop();
		} else {
			SteppingMotorControl::rotate(false);
		}
		minStep = maxStep = 0;
		modeState = 1;
		break;
	case 1:
		if (endPositionButton->getState() && (SteppingMotorControl::getStep() < 10)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			SteppingMotorControl::stop();
		} else if (startPositionButton->getState()) {
			modeState = 2;
			SteppingMotorControl::stop();
			SteppingMotorControl::resetStepTo(0);
		}
		break;
	case 2:
		mode = SteppingMotorControlIdle;
		modeState = 0;
		break;
	}
}

void SteppingMotorControlWithButtons::doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
	case 1:
		// These steps are absolutely the same.
		doInitializeToStartingPosition();
		break;
	case 2:
		// Rotate forward till the button gets pressed.
		SteppingMotorControl::rotate(true);
		modeState = 3;
		break;
	case 3:
		if (startPositionButton->getState() && (SteppingMotorControl::getStep() > 10)) {
			// The motor moved 10 steps and startButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			SteppingMotorControl::stop();
		} else if (endPositionButton->getState()) {
			// The end position moving forward is reached.
			maxStep = SteppingMotorControl::getStep();
			SteppingMotorControl::stop();
			SteppingMotorControl::rotate(false);
			modeState = 4;
		}
		break;
	case 4:
		if (endPositionButton->getState() && (maxStep - SteppingMotorControl::getStep() > 10)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			SteppingMotorControl::stop();
		} else if (startPositionButton->getState()) {
			minStep = SteppingMotorControl::getStep();
			SteppingMotorControl::stop();
			SteppingMotorControl::resetStepTo(0);
			mode = SteppingMotorControlIdle;
			modeState = 0;
		}
		break;
	}
}

void SteppingMotorControlWithButtons::update() {
	switch (mode) {
	case SteppingMotorControlInitializeToStartingPosition:
		doInitializeToStartingPosition();
		break;
	case SteppingMotorControlDetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	case SteppingMotorControlError:
		if (SteppingMotorControl::isMoving()) {
			stop();
		}
		break;
	default:
		if (startPositionButton->getState() || endPositionButton->getState()) {
			SteppingMotorControl::stop();
		}
		MIN(minStep, SteppingMotorControl::getStep());
		MAX(maxStep, SteppingMotorControl::getStep());
		break;
	}
	SteppingMotorControl::update();
}

void SteppingMotorControlWithButtons::initializeToStartingPosition() {
	mode = SteppingMotorControlInitializeToStartingPosition;
	modeState = 0;
}

void SteppingMotorControlWithButtons::determineAvailableSteps() {
	mode = SteppingMotorControlDetermineAvailableSteps;
	modeState = 0;
}
