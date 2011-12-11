#include "Arduino.h"
#include "Stepper.h"

void StepperMotorBA6845FS::initialize(
			StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;
	currentState = motorMode;
	stop();
}

void StepperMotorBA6845FS::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
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

/**
 * Half power consumption, lower torque
 */
static const uint8_t motorStatesBA6845FS_HalfPower[] = {
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b00011  // H-bridge 1 - Stop,    H-bridge 2 - Reverse
};

/**
 * Full power consumption & torque
 */
static const uint8_t motorStatesBA6845FS_FullPower[] = {
		0b00111, // H-bridge 1 - Forward, H-bridge 2 - Reverse
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01111  // H-bridge 1 - Reverse, H-bridge 2 - Reverse
};

/**
 * Higher (double) precision, variable power consumption & torque
 */
static const uint8_t motorStatesBA6845FS_DoublePrecision[] = {
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b01111, // H-bridge 1 - Reverse, H-bridge 2 - Reverse
		0b00011, // H-bridge 1 - Stop,    H-bridge 2 - Reverse
		0b00111  // H-bridge 1 - Forward, H-bridge 2 - Reverse
};

void StepperMotorBA6845FS::step(const bool moveForward) {
	char mode = currentState & 0b00000011;
	char index = currentState >> 2;
	char maxSteps = mode == StepperMotor::DoublePrecision ? 8 : 4;
	if (moveForward) {
		index++;
		if (index >= maxSteps)
			index = 1;
	} else {
		index--;
		if (index < 0)
			index = maxSteps - 1;
	}
	currentState = (index << 2) | mode;
	uint8_t state;
	switch (mode) {
	case DoublePrecision:
		state = motorStatesBA6845FS_DoublePrecision[index];
		break;
	case FullPower:
		state = motorStatesBA6845FS_FullPower[index];
		break;
	case HalfPower:
	default:
		state = motorStatesBA6845FS_HalfPower[index];
		break;
	}
	setState(state);
}

void StepperMotorBA6845FS::stop() {
	setState(0);
}

////////////////////////////////////////////

void StepperMotorMosfetHBridge::initialize(
		StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;
	currentState = motorMode;
	stop();
}

void StepperMotorMosfetHBridge::setState(const uint8_t state) {
	out11pin->setState(state & 0b01000);
	out12pin->setState(state & 0b00100);
	out21pin->setState(state & 0b00010);
	out22pin->setState(state & 0b00001);
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

void StepperMotorMosfetHBridge::step(const bool moveForward) {
	char mode = currentState & 0b00000011;
	char index = currentState >> 2;
	char maxSteps = mode == StepperMotor::DoublePrecision ? 8 : 4;
	if (moveForward) {
		index++;
		if (index >= maxSteps)
			index = 1;
	} else {
		index--;
		if (index < 0)
			index = maxSteps - 1;
	}
	currentState = (index << 2) | mode;
	uint8_t state;
	switch (mode) {
	case DoublePrecision:
		state = motorStatesMosfetHBridge_DoublePrecision[index];
		break;
	case FullPower:
		state = motorStatesMosfetHBridge_FullPower[index];
		break;
	case HalfPower:
	default:
		state = motorStatesMosfetHBridge_HalfPower[index];
		break;
	}
	setState(state);
}

void StepperMotorMosfetHBridge::stop() {
	setState(0);
}

////////

void StepperMotorControlWithButtons::initialize(
		StepperMotor *motor,
		DigitalInputPin *startButton,
		DigitalInputPin *endButton) {
	this->motor = motor;
	this->startButton = startButton;
	this->endButton = endButton;
	currentStep = 0;
	movementMode = StepperMotorControlWithButtons::Idle;
	delayBetweenStepsMicros = 2000UL;
}

void StepperMotorControlWithButtons::update() {
	bool moveForeward;
	switch (movementMode) {
	case Idle:
		return;
	case Backward:
	case GotoStartButton:
		moveForeward = false;
		break;
	// case Foreward:
	// case GotoEndButton:
	default:
		moveForeward = true;
		break;
	}

	unsigned long now = micros();
	// Stopping & End buttons check
	if ((movementMode == StepperMotorControlWithButtons::Stopping) ||
		(moveForeward && !endButton->getState()) ||
		(!moveForeward && !startButton->getState())) {
		if (now - lastTimestampMicros >= timeToStopMicros) {
			motor->stop();
			movementMode = StepperMotorControlWithButtons::Idle;
			if (!moveForeward)
				currentStep = 0;
		}
		return;
	}

	// Speed control
	if (now - lastTimestampMicros < delayBetweenStepsMicros)
		return;

	if ((movementMode == StepperMotorControlWithButtons::Foreward) ||
		(movementMode == StepperMotorControlWithButtons::Backward)) {
		if (remainingSteps == 0) {
			movementMode = StepperMotorControlWithButtons::Stopping;
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

void StepperMotorControlWithButtons::gotoStep(long step) {
	step -= currentStep;
	if (step >= 0) {
		movementMode = StepperMotorControlWithButtons::Foreward;
		remainingSteps = step;
	} else {
		movementMode = StepperMotorControlWithButtons::Backward;
		remainingSteps = -step;
	}
	lastTimestampMicros = millis() - delayBetweenStepsMicros;
}

void StepperMotorControlWithButtons::rotate(bool foreward) {
	movementMode = foreward ? StepperMotorControlWithButtons::Foreward : StepperMotorControlWithButtons::Backward;
	lastTimestampMicros = millis() - delayBetweenStepsMicros;
}

void StepperMotorControlWithButtons::stop() {
	if (movementMode != StepperMotorControlWithButtons::Idle) {
		movementMode = StepperMotorControlWithButtons::Stopping;
	}
}

///////

void StepperMotorAxis::initialize(StepperMotor *motor,
		DigitalInputPin *startButton,
		DigitalInputPin *endButton) {
	motorControl.initialize(motor, startButton, endButton);
	homePositionMM = -32767;
	delayBetweenStepsAtMaxSpeedMicros = 2000;
	axisResolution = 1000;
	mode = StepperMotorAxis::Idle;
}

void StepperMotorAxis::update() {
	motorControl.update();
	switch (mode) {
	case DetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	}
}

void StepperMotorAxis::doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
		motorControl.setDelayBetweenStepsMicros(delayBetweenStepsAtMaxSpeedMicros);
		motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if (!motorControl.isMoving()) {
			motorControl.rotate(true);
			modeState = 2;
		}
		break;
	case 2:
		if (!motorControl.isMoving()) {
			mode = StepperMotorAxis::Idle;
		}
		break;
	}
}

void StepperMotorAxis::determineAvailableSteps(void) {
	mode = StepperMotorAxis::DetermineAvailableSteps;
	modeState = 0;
}

void StepperMotorAxis::moveToPositionMicroM(long absolutePositionMicroM, unsigned long delayBetweenStepsMicros) {
	motorControl.setDelayBetweenStepsMicros(delayBetweenStepsMicros);
	motorControl.gotoStep(absolutePositionMicroM * 100000L / ((long) axisResolution));
}

long StepperMotorAxis::getAbsolutePositionMicroM() {
	return motorControl.getStep() * 100000L / ((long) axisResolution);
}

