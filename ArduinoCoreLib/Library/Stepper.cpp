#define Stepper_h
#ifndef Stepper_h

#include "Arduino.h"
#include "Stepper.h"

void StepperMosfetHBridge::initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;
	currentState = 0;
	stop();
}

static const uint8_t motorStatesMosfetHBridge[] = {
		0b01000,
		0b00010,
		0b00100,
		0b00001
};

void StepperMosfetHBridge::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
}

void StepperMosfetHBridge::step(const bool moveForward) {
	if (moveForward) {
		currentState++;
		if (currentState >= size(motorStatesMosfetHBridge))
			currentState = 1;
	} else {
		currentState--;
		if (currentState < 0)
			currentState = size(motorStatesMosfetHBridge) - 1;
	}
	setState(motorStatesMosfetHBridge[currentState]);
	return true;
}

void SteppingMotor_MosfetHBridge::stop() {
	setState(0);
}

////////

void StepperMosfetHBridgeWithLengthControl::initialize(
		DigitalOutputPin *out11pin,
		DigitalOutputPin *out12pin,
		DigitalOutputPin *out21pin,
		DigitalOutputPin *out22pin) {
	StepperMosfetHBridge::initialize(out11pin, out12pin, out21pin, out22pin);
	remainingSteps = 0;
}

bool StepperMosfetHBridgeWithLengthControl::step(const bool moveForward) {
	return remainingSteps == 0 ? false : StepperMosfetHBridge::step(moveForward);
}

void StepperMosfetHBridgeWithLengthControl::stop() {
	StepperMosfetHBridge::stop();
	remainingSteps = 0;
}

////////

void StepperMosfetHBridgeWithButtons::initialize(
		DigitalOutputPin *out11pin,
		DigitalOutputPin *out12pin,
		DigitalOutputPin *out21pin,
		DigitalOutputPin *out22pin,
		DigitalInputPin *startButton,
		DigitalInputPin *endButton) {
	StepperMosfetHBridgeWithLengthControl::initialize(out11pin, out12pin, out21pin, out22pin);
	this->startButton = startButton;
	this->endButton = endButton;
}

bool StepperMosfetHBridgeWithButtons::step(const bool moveForward) {
	if (((moveForward && !endButton->getState()) ||
		(!moveForward && !startButton->getState())))
		return false;
	if (StepperMosfetHBridgeWithLengthControl::step(moveForward)) {
		if (moveForward)
			currentStep++;
		else
			currentStep--;
		return true;
	}
	return false;
}

////////

void SteperSpeedControl::initialize(StepperMotor * motor) {
	this->motor = motor;
	stepAtMicros = 0;
}

void SteperSpeedControl::update() {
	if (movementMode == 0) {
		return;
	}
	unsigned long now = micros();
	if (now - stepAtMicros < delayBetweenStepsMicros)
		return;
	if (motor->step(movementMode == 1)) {
		stepAtMicros += delayBetweenStepsMicros;
	} else {
		stop();
	}
}

void SteperSpeedControl::move(bool foreward) {
	uint8_t newMode = foreward ? 1 : 2;
	if (movementMode == newMode)
		return;
	movementMode = newMode;
	stepAt = micros();
}

void SteperSpeedControl::stop() {
	if (movementMode != 0) {
		movementMode = 0;
		motor->stop();
	}
}

////////

void StepperAxis::initialize(
		DigitalInputPin *startButton,
		DigitalInputPin *endButton) {
	this->motor = motor;
	this->startButton = startButton;
	this->endButton = endButton;
}

void StepperAxis::update() {
	bool moveForeward;
	switch (movementMode) {
	case Idle:
		return;
	case Backward:
	case GotoStartButton:
		moveForeward = false;
		break;
//	case Foreward:
//	case GotoEndButton:
	default:
		moveForeward = true;
		break;
	}

	unsigned long now = micros();
	// Stopping & End buttons check
	if ((movementMode == StepperAxis::Stopping) ||
		(moveForeward && !endButton->getState()) ||
		(!moveForeward && !startButton->getState())) {
		if (now - lastTimestampMicros >= timeToStopMicros) {
			motor->stop();
			movementMode = StepperAxis::Idle;
			if (!moveForeward)
				currentStep = 0;
		}
		return;
	}

	// Speed control
	if (now - lastTimestampMicros < delayBetweenStepsMicros)
		return;

	if ((movementMode == StepperAxis::Foreward) ||
		(movementMode == StepperAxis::Backward)) {
		if (remainingSteps == 0) {
			movementMode = StepperAxis::Stopping;
			return;
		}
		remainingSteps--;
	}

	if (moveForeward) {
		currentStep++;
	} else {
		currentStep--;
	}
	motor->step(moveForeward);
	lastTimestampMicros += delayBetweenStepsMicros; // should increase, not set to now!
}

void StepperAxis::gotoStep(long step) {
	step -= currentStep;
	if (step >= 0) {
		movementMode = StepperAxis::Foreward;
		remainingSteps = step;
	} else {
		movementMode = StepperAxis::Backward;
		remainingSteps = -step;
	}
	lastTimestampMicros = millis() - delayBetweenStepsMicros;
}

void StepperAxis::rotate(bool foreward) {
	movementMode = foreward ? StepperAxis::Foreward : StepperAxis::Backward;
	lastTimestampMicros = millis() - delayBetweenStepsMicros;
}

void StepperAxis::stop() {
	if (movementMode != StepperAxis::Idle) {
		movementMode = StepperAxis::Stopping;
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
	activateUpdater();
}

void SteppingMotorControl::stop() {
	motor->stop();
	movementMode = 0;
	targetStep = step;
	motorCoilOnMicros = micros() - delayBetweenStepsMicros;
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
		(micros() - motorCoilOnMicros > delayBetweenStepsMicros));
}

void SteppingMotorControl::update() {
	unsigned long now = micros();
	if (now - motorCoilOnMicros >= delayBetweenStepsMicros) {
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

#define MaxStepsWithWrongButtonDown 50
void SteppingMotorControlWithButtons::doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		// Start moving backward to begining
		motorControl.resetStepTo(0);
		if (startPositionButton->getState()) {
			// button is up
			motorControl.rotate(false);
			Serial.print("start btn up step=");
			Serial.println(motorControl.getStep());
		} else {
			// button is down
			motorControl.stop();
			Serial.print("start btn down step=");
			Serial.println(motorControl.getStep());
		}
		minStep = maxStep = 0;
		modeState = 1;
		break;
	case 1:
		if ((!endPositionButton->getState()) && (motorControl.getStep() <= -MaxStepsWithWrongButtonDown)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
			Serial.println("err1 step=");
			Serial.println(motorControl.getStep());
		} else if (!startPositionButton->getState()) {
			modeState = 2;
			motorControl.stop();
			motorControl.resetStepTo(0);
			Serial.print("start btn down 2 step=");
			Serial.println(motorControl.getStep());
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
		if ((!startPositionButton->getState()) && (motorControl.getStep() >= MaxStepsWithWrongButtonDown)) {
			// The motor moved 10 steps and startButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
			Serial.print("err2 step=");
			Serial.println(motorControl.getStep());
		} else if (!endPositionButton->getState()) {
			// The end position moving forward is reached.
			maxStep = motorControl.getStep();
			motorControl.stop();
			motorControl.rotate(false);
			modeState = 4;
			Serial.print("end btn down step=");
			Serial.println(motorControl.getStep());
		}
		break;
	case 4:
		if ((!endPositionButton->getState()) && (maxStep - motorControl.getStep() >= MaxStepsWithWrongButtonDown)) {
			// The motor moved 10 steps and endButton is still down - error
			mode = SteppingMotorControlError;
			modeState = 0;
			motorControl.stop();
			Serial.print("err3 step=");
			Serial.println(motorControl.getStep());
		} else if (!startPositionButton->getState()) {
			minStep = motorControl.getStep();
			motorControl.stop();
			motorControl.resetStepTo(0);
			mode = SteppingMotorControlIdle;
			modeState = 0;
			Serial.print("start btn down 3 step=");
			Serial.println(motorControl.getStep());
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

#endif
