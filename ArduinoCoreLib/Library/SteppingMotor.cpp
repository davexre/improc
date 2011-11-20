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
			SteppingMotor::SteppingMotorMode steppingMotorMode,
			StepSwitchingMode stepSwitchingMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->steppingMotorMode = steppingMotorMode;
	this->stepSwitchingMode = stepSwitchingMode;
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

/**
 * Full power consumption & torque
 */
static const uint8_t motorStatesMosfetHBridge_FullPower[] = {
		0b01010,
		0b00110,
		0b00101,
		0b01001
};

/**
 * Half power consumption, lower torque
 */
static const uint8_t motorStatesMosfetHBridge_HalfPower[] = {
		0b01000,
		0b00010,
		0b00100,
		0b00001
};

/**
 * Higher (double) precision, variable power consumption & torque
 */
static const uint8_t motorStatesMosfetHBridge_DoublePrecision[] = {
		0b01000,
		0b01001,
		0b00010,
		0b01010,
		0b00100,
		0b00110,
		0b00001,
		0b00101
};

void SteppingMotor_MosfetHBridge::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
}

void SteppingMotor_MosfetHBridge::step(const bool moveForward) {
	uint8_t statesSize;
	switch (steppingMotorMode) {
	case DoublePrecision:
		statesSize = size(motorStatesMosfetHBridge_DoublePrecision);
		break;
	case FullPower:
		statesSize = size(motorStatesMosfetHBridge_FullPower);
		break;
	case HalfPower:
	default:
		statesSize = size(motorStatesMosfetHBridge_HalfPower);
		break;
	}

	if (moveForward) {
		currentState++;
		if (currentState >= statesSize)
			currentState = 0;
	} else {
		currentState--;
		if (currentState < 0)
			currentState = statesSize - 1;
	}
	switch (stepSwitchingMode) {
	case DoNotTurnOff:
		mode = 3;
		break;
	case TurnOffInSameCycle:
		mode = 2;
		break;
	case TurnOffInSeparateCycle:
	default:
		mode = 1;
		break;
	}
}

void SteppingMotor_MosfetHBridge::stop() {
	mode = 0;
	setState(0); // Turn all Mosfets off
}

void SteppingMotor_MosfetHBridge::update() {
	switch (mode) {
	case 1:
		// Before switching HBridge turn off all Mosfets
		setState(0); // Turn all Mosfets off
		mode = 3;
		break;
	case 2:
		setState(0); // Turn all Mosfets off
		// No break here. Continue on with case 3
	case 3:
		uint8_t state;
		switch (steppingMotorMode) {
		case DoublePrecision:
			state = motorStatesMosfetHBridge_DoublePrecision[currentState];
			break;
		case FullPower:
			state = motorStatesMosfetHBridge_FullPower[currentState];
			break;
		case HalfPower:
		default:
			state = motorStatesMosfetHBridge_HalfPower[currentState];
			break;
		}
		setState(state);
		motorCoilOnMicros = micros();
		mode = 4;
		break;
	case 4:
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
	stepsMadeSoFar = 0;
	minDelayBetweenStepsMicros = 1000;
	step = 0;
	motor->stop();
}

void SteppingMotorControl::stop() {
	if ((movementMode == SteppingMotorControl::Idle) ||
		(movementMode == SteppingMotorControl::Stopping))
		return;
	movementMode = SteppingMotorControl::Stopping;
	stepAtMicros = micros() + minDelayBetweenStepsMicros;
}

void SteppingMotorControl::gotoStep(const long step) {
	movementMode = SteppingMotorControl::GotoStepWithDelayBetweenSteps;
	targetStep = step;
	stepAtMicros = micros();
}

void SteppingMotorControl::gotoStepInMicros(const long step, const unsigned long timeToStepMicros) {
	movementMode = SteppingMotorControl::GotoStepWithTimePeriod;
	targetStep = step;
	timeMicros = timeToStepMicros;
	stepAtMicros = micros();
}

void SteppingMotorControl::gotoStepInFixedTimeVariableSpeed(const long step) {
	movementMode = SteppingMotorControl::GotoStepInFixedTimeVariableSpeed;
	targetStep = step;
	timeMicros = stepAtMicros = micros();
}

void SteppingMotorControl::rotate(const bool forward) {
	if (movementMode == SteppingMotorControl::Idle) {
		// If the motor is idle - start moving immediately, otherwise change the mode,
		// but wait for the step to finish.
		stepAtMicros = micros();
	}
	movementMode = forward ? SteppingMotorControl::ContinuousForward : SteppingMotorControl::ContinuousBackward;
}

void SteppingMotorControl::resetStepTo(const long step) {
	stop();
	this->step = step;
}

bool SteppingMotorControl::isMoving() {
	return movementMode != SteppingMotorControl::Idle;
}

void SteppingMotorControl::update() {
	if (movementMode == SteppingMotorControl::Idle)
		return;

	unsigned long now = micros();
	unsigned long dt = now - stepAtMicros;

	if ((signed long)dt < 0)
		return;

	switch (movementMode) {
	case GotoStepWithDelayBetweenSteps:
	case GotoStepWithTimePeriod:
	case GotoStepInFixedTimeVariableSpeed:
		break;
	case Stopping:
		motor->stop();
		movementMode = SteppingMotorControl::Idle;
		return;
	case ContinuousForward:
		motor->step(true);
		step++;
		stepsMadeSoFar++;
		stepAtMicros = now + delayBetweenStepsMicros;
		return;
	case ContinuousBackward:
		motor->step(false);
		step--;
		stepsMadeSoFar++;
		stepAtMicros = now + delayBetweenStepsMicros;
		return;
	default:
		return;
	}

	long stepsRemaining = targetStep - step;
	if (stepsRemaining == 0) {
		stop();
		return;
	}
	if (stepsRemaining > 0) {
		motor->step(true);
		step++;
		stepsRemaining--;
	} else { // if (stepsRemaining < 0) {
		motor->step(false);
		step--;
		stepsRemaining = 1 - stepsRemaining;
	}
	stepsMadeSoFar++;
	if (stepsRemaining == 0) {
		stop();
		return;
	}

	switch (movementMode) {
	case GotoStepWithDelayBetweenSteps:
		stepAtMicros = now + delayBetweenStepsMicros;
		break;
	case GotoStepWithTimePeriod:
		timeMicros -= dt;
		stepAtMicros = now + timeMicros/stepsRemaining;
		break;
	case GotoStepInFixedTimeVariableSpeed:
		timeMicros += delayBetweenStepsMicros;
		if (timeMicros - now < minDelayBetweenStepsMicros)
			stepAtMicros = timeMicros + minDelayBetweenStepsMicros;
		else
			stepAtMicros = timeMicros;
		break;
	}
}

////////

void SteppingMotorControlWithButtons::initialize(SteppingMotor *motor,
		DigitalInputPin *startPositionButtonPin,
		DigitalInputPin *endPositionButtonPin) {
	motorControl.initialize(motor);
	startPositionButton = startPositionButtonPin;
	endPositionButton = endPositionButtonPin;
	mode = SteppingMotorControlWithButtons::Idle;
	modeState = 0;
	minStep = 0;
	maxStep = 0;
	maxStepsWithWrongButtonDown = 50;
}

void SteppingMotorControlWithButtons::gotoStep(const long step) {
//	motorControl.gotoStepInFixedTimeVariableSpeed(step);
	motorControl.gotoStep(step);
	mode = SteppingMotorControlWithButtons::Waiting;
	modeState = 0;
}

void SteppingMotorControlWithButtons::rotate(const bool forward) {
	motorControl.rotate(forward);
	mode = SteppingMotorControlWithButtons::Waiting;
	modeState = 0;
}

void SteppingMotorControlWithButtons::stop() {
	motorControl.stop();
	mode = SteppingMotorControlWithButtons::Waiting;
	modeState = 0;
}

void SteppingMotorControlWithButtons::doInitializeToStartPosition() {
	switch (modeState) {
	case 0:
		// Start moving backward to begining
		motorControl.resetStepTo(0);
		if (startPositionButton->getState()) {
			// button is up
			motorControl.rotate(false);
		} else {
			// button is down
			motorControl.stop();
		}
		minStep = maxStep = 0;
		modeState = 1;
		break;
	case 1:
		if ((!endPositionButton->getState()) && (motorControl.getStep() <= -maxStepsWithWrongButtonDown)) {
			// The motor moved maxStepsWithWrongButtonDown steps and endButton is still down - error
			mode = SteppingMotorControlWithButtons::Error;
			modeState = 0;
			motorControl.stop();
		} else if (!startPositionButton->getState()) {
			modeState = 2;
			motorControl.stop();
			motorControl.resetStepTo(0);
		}
		break;
	case 2:
		mode = SteppingMotorControlWithButtons::Waiting;
		modeState = 0;
		break;
	}
}

void SteppingMotorControlWithButtons::doInitializeToEndPosition() {
	switch (modeState) {
	case 0:
		motorControl.resetStepTo(0);
		if (endPositionButton->getState()) {
			// button is up
			// Start moving foreward to the end of the axis
			motorControl.rotate(true);
		}
		modeState = 1;
		break;
	case 1:
		if ((!startPositionButton->getState()) && (motorControl.getStep() <= -maxStepsWithWrongButtonDown)) {
			// The motor moved maxStepsWithWrongButtonDown steps and startButton is still down - error
			mode = SteppingMotorControlWithButtons::Error;
			modeState = 0;
			motorControl.stop();
		} else if (!endPositionButton->getState()) {
			modeState = 2;
			motorControl.stop();
			motorControl.resetStepTo(maxStep);
		}
		break;
	case 2:
		mode = SteppingMotorControlWithButtons::Waiting;
		modeState = 0;
		break;
	}
}

void SteppingMotorControlWithButtons::doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
	case 1:
		// These steps are absolutely the same.
		doInitializeToStartPosition();
		break;
	case 2:
		// Rotate forward till the button gets pressed.
		motorControl.rotate(true);
		modeState = 3;
		break;
	case 3:
		if ((!startPositionButton->getState()) && (motorControl.getStep() >= maxStepsWithWrongButtonDown)) {
			// The motor moved maxStepsWithWrongButtonDown steps and startButton is still down - error
			mode = SteppingMotorControlWithButtons::Error;
			modeState = 0;
			motorControl.stop();
		} else if (!endPositionButton->getState()) {
			// The end position moving forward is reached.
			maxStep = motorControl.getStep();
			motorControl.stop();
			motorControl.rotate(false);
			modeState = 4;
		}
		break;
	case 4:
		if ((!endPositionButton->getState()) && (maxStep - motorControl.getStep() >= maxStepsWithWrongButtonDown)) {
			// The motor moved maxStepsWithWrongButtonDown steps and endButton is still down - error
			mode = SteppingMotorControlWithButtons::Error;
			modeState = 0;
			motorControl.stop();
		} else if (!startPositionButton->getState()) {
			minStep = motorControl.getStep();
			motorControl.stop();
			motorControl.resetStepTo(0);
			mode = SteppingMotorControlWithButtons::Waiting;
			modeState = 0;
		}
		break;
	}
}

void SteppingMotorControlWithButtons::update() {
	switch (mode) {
	case InitializeToStartPosition:
		doInitializeToStartPosition();
		break;
	case InitializeToEndPosition:
		doInitializeToEndPosition();
		break;
	case DetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	case Idle:
	case Error:
		if (motorControl.isMoving()) {
			motorControl.stop();
		}
		break;
	case Waiting:
		if (!motorControl.isMoving()) {
			mode = SteppingMotorControlWithButtons::Idle;
			modeState = 0;
		}
		// No break here.
	default:
		if ((!startPositionButton->getState()) || (!endPositionButton->getState())) {
			// start or end button is down
			motorControl.stop();
		}
		MIN(minStep, motorControl.getStep());
		MAX(maxStep, motorControl.getStep());
		break;
	}
	motorControl.update();
}

void SteppingMotorControlWithButtons::initializeToStartPosition() {
	mode = SteppingMotorControlWithButtons::InitializeToStartPosition;
	modeState = 0;
}

void SteppingMotorControlWithButtons::initializeToEndPosition() {
	mode = SteppingMotorControlWithButtons::InitializeToEndPosition;
	modeState = 0;
}

void SteppingMotorControlWithButtons::determineAvailableSteps() {
	mode = SteppingMotorControlWithButtons::DetermineAvailableSteps;
	modeState = 0;
}

bool SteppingMotorControlWithButtons::isMoving() {
	return ((mode != SteppingMotorControlWithButtons::Idle) && (mode != SteppingMotorControlWithButtons::Error)) ||
		(motorControl.isMoving());
}
