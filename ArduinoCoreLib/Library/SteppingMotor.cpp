#include "Arduino.h"
#include "SteppingMotor.h"

void SteppingMotor_BA6845FS::initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;
	currentState = 0;
	motorCoilTurnOffMicros = 1000;
	motorCoilOnMicros = 0;
	isMotorCoilOn = false;
	stop();
}

// http://www.engineersgarage.com/articles/stepper-motors

/*
	BA6845FS Truth table
	IN11/21  IN12/22  OUT11/21  OUT12/22  MODE
	LOW      HIGH     HIGH      LOW       Forward
	HIGH     HIGH     LOW       HIGH      Reverse
	LOW      LOW      OPEN      OPEN      Stop
	HIGH     HIGH     OPEN      OPEN      Stop
*/

// Wave drive
static const uint8_t motorStates[] = {
		0b00000, // OFF
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b00011  // H-bridge 1 - Stop,    H-bridge 2 - Reverse
};

/*
// Full drive
static const uint8_t motorStates[] = {
		0b00000, // OFF
		0b00111, // H-bridge 1 - Forward, H-bridge 2 - Reverse
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01111  // H-bridge 1 - Reverse, H-bridge 2 - Reverse
};

// Half drive
static const uint8_t motorStates[] = {
		0b00000, // OFF
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b01111  // H-bridge 1 - Reverse, H-bridge 2 - Reverse
		0b00011  // H-bridge 1 - Stop,    H-bridge 2 - Reverse
		0b00111, // H-bridge 1 - Forward, H-bridge 2 - Reverse
};

*/

void SteppingMotor_BA6845FS::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
}

void SteppingMotor_BA6845FS::step(const bool moveForward) {
	if (moveForward) {
		currentState++;
		if (currentState >= size(motorStates))
			currentState = 1;
	} else {
		currentState--;
		if (currentState <= 0)
			currentState = size(motorStates) - 1;
	}
	isMotorCoilOn = true;
	setState(motorStates[currentState]);
	motorCoilOnMicros = micros();
}

void SteppingMotor_BA6845FS::stop() {
	isMotorCoilOn = false;
	setState(motorStates[0]);
}

void SteppingMotor_BA6845FS::update() {
	if (isMotorCoilOn) {
		if (micros() - motorCoilOnMicros > motorCoilTurnOffMicros) {
			stop();
		}
	}
}

//////////

void SteppingMotor_MosfetHBridge::initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;
	currentState = 0;
	motorCoilTurnOffMicros = 1000;
	motorCoilOnMicros = 0;
	mode = 0;
	stop();
}

static const uint8_t motorStatesMosfetHBridge[] = {
		0b00000, // OFF
		0b01000,
		0b00010,
		0b00100,
		0b00001
};

void SteppingMotor_MosfetHBridge::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
}

void SteppingMotor_MosfetHBridge::step(const bool moveForward) {
	if (moveForward) {
		currentState++;
		if (currentState >= size(motorStatesMosfetHBridge))
			currentState = 1;
	} else {
		currentState--;
		if (currentState <= 0)
			currentState = size(motorStatesMosfetHBridge) - 1;
	}
	mode = 1;
}

void SteppingMotor_MosfetHBridge::stop() {
	mode = 0;
	setState(motorStatesMosfetHBridge[0]);
}

void SteppingMotor_MosfetHBridge::update() {
	switch (mode) {
	case 1:
		// Before switching HBridge turn off all Mosfets
		setState(motorStatesMosfetHBridge[0]);
		mode = 2;
		break;
	case 2:
		setState(motorStatesMosfetHBridge[currentState]);
		motorCoilOnMicros = micros();
		mode = 3;
		break;
	case 3:
		if (micros() - motorCoilOnMicros > motorCoilTurnOffMicros) {
			stop();
			mode = 0;
		}
		break;
	case 0:
	default:
		break; // do nothing
	}
}

////////

void SteppingMotorControl::initialize(SteppingMotor *motor) {
	this->motor = motor;
	delayBetweenStepsMicros = 2000UL;
	motorCoilOnMicros = 0;
	stepsMadeSoFar = 0;
	step = 0;
	motor->stop();
}

void SteppingMotorControl::stop() {
	motor->stop();
	movementMode = 0;
	targetStep = step;
}

void SteppingMotorControl::gotoStep(const long step) {
	movementMode = 0;
	targetStep = step;
}

void SteppingMotorControl::rotate(const bool forward) {
	movementMode = forward ? 1 : 2;
}

void SteppingMotorControl::resetStepTo(const long step) {
	this->movementMode = 0;
	this->step = this->targetStep = step;
}

bool SteppingMotorControl::isMoving() {
	return !(
		(movementMode == 0) &&
		(targetStep == step) &&
		(micros() - motorCoilOnMicros <= delayBetweenStepsMicros));
}

void SteppingMotorControl::update() {
	unsigned long now = micros();
	if (now - motorCoilOnMicros > delayBetweenStepsMicros) {
		bool shallMove;
		bool forward;

		switch (movementMode) {
		case 1:
			shallMove = true;
			forward = true;
			break;
		case 2:
			shallMove = true;
			forward = false;
			break;
		default: //	case 0:
			long tmp = targetStep - step;
			if (tmp == 0) {
				shallMove = false;
				forward = false;
			} else {
				shallMove = true;
				if (tmp > 0) {
					forward = true;
				} else {
					forward = false;
				}
			}
			break;
		}

		if (shallMove) {
			if (forward) {
				step++;
			} else {
				step--;
			}
			motor->step(forward);
			stepsMadeSoFar++;
			motorCoilOnMicros = now;
		} else {
			motorCoilOnMicros = now - delayBetweenStepsMicros;
		}
	}
}

////////

void SteppingMotorControlWithButtons::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	motorControl.initialize(motor);
	startPositionButton = startPositionButtonPin;
	endPositionButton = endPositionButtonPin;
	mode = SteppingMotorControlIdle;
	modeState = 0;
	minStep = 0;
	maxStep = 0;
}

void SteppingMotorControlWithButtons::gotoStep(const long step) {
	motorControl.gotoStep(step);
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::rotate(const bool forward) {
	motorControl.rotate(forward);
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::stop() {
	motorControl.stop();
	mode = SteppingMotorControlIdle;
	modeState = 0;
}

void SteppingMotorControlWithButtons::doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		// Start moving backward to begining
		motorControl.resetStepTo(0);
		if (startPositionButton->getState()) {
			motorControl.stop();
		} else {
			motorControl.rotate(false);
		}
		minStep = maxStep = 0;
		modeState = 1;
		break;
	case 1:
		if (endPositionButton->getState() && (motorControl.getStep() < 10)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
		} else if (startPositionButton->getState()) {
			modeState = 2;
			motorControl.stop();
			motorControl.resetStepTo(0);
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
		motorControl.rotate(true);
		modeState = 3;
		break;
	case 3:
		if (startPositionButton->getState() && (motorControl.getStep() > 10)) {
			// The motor moved 10 steps and startButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
		} else if (endPositionButton->getState()) {
			// The end position moving forward is reached.
			maxStep = motorControl.getStep();
			motorControl.stop();
			motorControl.rotate(false);
			modeState = 4;
		}
		break;
	case 4:
		if (endPositionButton->getState() && (maxStep - motorControl.getStep() > 10)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
		} else if (startPositionButton->getState()) {
			minStep = motorControl.getStep();
			motorControl.stop();
			motorControl.resetStepTo(0);
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
		if (motorControl.isMoving()) {
			stop();
		}
		break;
	default:
		if (startPositionButton->getState() || endPositionButton->getState()) {
			motorControl.stop();
		}
		MIN(minStep, motorControl.getStep());
		MAX(maxStep, motorControl.getStep());
		break;
	}
	motorControl.update();
}

void SteppingMotorControlWithButtons::initializeToStartingPosition() {
	mode = SteppingMotorControlInitializeToStartingPosition;
	modeState = 0;
}

void SteppingMotorControlWithButtons::determineAvailableSteps() {
	mode = SteppingMotorControlDetermineAvailableSteps;
	modeState = 0;
}

bool SteppingMotorControlWithButtons::isMoving() {
	return ((mode != SteppingMotorControlIdle) && (mode != SteppingMotorControlError)) ||
		(motorControl.isMoving());
}
