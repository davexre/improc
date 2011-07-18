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
	setState(motorStates[currentState]);
	motorCoilOnMicros = micros();
}

void SteppingMotor_BA6845FS::stop() {
	isMotorCoilOn = false;
	setState(motorStates[0]);
}

void SteppingMotor_BA6845FS::update() {
	if (isMotorCoilOn) {
		if (micros() - motorCoilOnMicros > motorCoilDelayBetweenStepsMicros) {
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
	isMotorCoilOn = false;
	stop();
}

////////

void SteppingMotorControll::initialize(SteppingMotor *motor) {
	this->motor = motor;
	motorCoilDelayBetweenStepsMicros = 2000;
	motorCoilOnMicros = 0;
	step = 0;
	motor->stop();
}

void SteppingMotorControll::stop() {
	motor->stop();
	movementMode = 0;
	targetStep = step;
}

void SteppingMotorControll::gotoStep(const long step) {
	movementMode = 0;
	targetStep = step;
}

void SteppingMotorControll::rotate(const bool forward) {
	movementMode = forward ? 1 : 2;
}

void SteppingMotorControll::resetStepTo(const long step) {
	this->movementMode = 0;
	this->step = this->targetStep = step;
}

bool SteppingMotorControll::isMoving() {
	return !(
		(movementMode == 0) &&
		(targetStep == step) &&
		(micros() - motorCoilOnMicros <= motorCoilDelayBetweenStepsMicros));
}

void SteppingMotorControll::update() {
	unsigned long now = micros();
	if (now - motorCoilOnMicros > motorCoilDelayBetweenStepsMicros) {
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
			motorCoilOnMicros = now;
		} else {
			motorCoilOnMicros = now - motorCoilDelayBetweenStepsMicros;
		}
	}
}
