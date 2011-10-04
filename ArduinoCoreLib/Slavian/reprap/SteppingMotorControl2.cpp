#include "Arduino.h"
#include "SteppingMotorControl2.h"

void SteppingMotorControlWithButtons::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	SteppingMotorControl::initialize(motor);
	startPositionButton.initialize(startPositionButtonPin);
	endPositionButton.initialize(endPositionButtonPin);
	mode = SteppingMotorControlIdle;
	modeState = 0;
	maxStep = -1;
}

void SteppingMotorControlWithButtons::gotoStep(const long step) {
	SteppingMotorControl::gotoStep(step);
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void rotate(const bool forward) {
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
		if (startPositionButton.isDown()) {
			SteppingMotorControl::stop();
		} else {
			SteppingMotorControl::rotate(false);
		}
		modeState = 1;
		break;
	case 1:
		if (endPositionButton.isDown() && (SteppingMotorControl::getStep() < 10)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			SteppingMotorControl::stop();
		} else if (startPositionButton.isDown()) {
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
		if (startPositionButton.isDown() && (SteppingMotorControl::getStep() > 10)) {
			// The motor moved 10 steps and startButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			SteppingMotorControl::stop();
		} else if (endPositionButton.isDown()) {
			// The end position moving forward is reached.
			SteppingMotorControl::stop();
			maxStep = SteppingMotorControl::getStep();
			mode = SteppingMotorControlIdle;
			modeState = 0;
		}
		break;
	}
}

void SteppingMotorControlWithButtons::update() {
	startPositionButton.update();
	endPositionButton.update();

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
		if (startPositionButton.isDown() || endPositionButton.isDown()) {
			SteppingMotorControl::stop();
		}
		break;
	}
	SteppingMotorControl::update();
}
