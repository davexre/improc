#include "Arduino.h"
#include "SteppingMotor.h"

void SteppingMotor::initialize(const uint8_t out11pin, const uint8_t out12pin, const uint8_t out21pin, const uint8_t out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;

	motorCoilTurnOffMicros = 1000;
	motorCoilDelayBetweenStepsMicros = 2000;
	isMotorOn = false;
	motorOnMicros = 0;
	currentState = 0;
	step = 0;

	pinMode(out11pin, OUTPUT);
	digitalWrite(out11pin, LOW);

	pinMode(out12pin, OUTPUT);
	digitalWrite(out12pin, LOW);

	pinMode(out21pin, OUTPUT);
	digitalWrite(out21pin, LOW);

	pinMode(out22pin, OUTPUT);
	digitalWrite(out22pin, LOW);
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
void SteppingMotor::setState(const uint8_t state) {
	digitalWrite(out11pin, state & 0b01000);
	digitalWrite(out12pin, state & 0b00100);
	digitalWrite(out21pin, state & 0b00010);
	digitalWrite(out22pin, state & 0b00001);
}

void SteppingMotor::stop() {
	setState(motorStates[0]);
	isMotorOn = false;
	movementMode = 0;
	targetStep = step;
}

void SteppingMotor::gotoStep(const long step) {
	movementMode = 0;
	targetStep = step;
}

void SteppingMotor::rotate(const boolean forward) {
	movementMode = forward ? 1 : 2;
}

void SteppingMotor::resetStepTo(const long step) {
	this->movementMode = 0;
	this->step = this->targetStep = step;
}

void SteppingMotor::update() {
	boolean shallMove;
	boolean forward;

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

	long now = micros();
	if (now - motorOnMicros >= motorCoilDelayBetweenStepsMicros) {
		if (shallMove) {
			if (forward) {
				step++;
				currentState++;
				if (currentState >= size(motorStates))
					currentState = 1;
			} else {
				step--;
				currentState--;
				if (currentState <= 0)
					currentState = size(motorStates) - 1;
			}
			setState(motorStates[currentState]);
			isMotorOn = true;
			motorOnMicros = now;
		} else {
			motorOnMicros = now - motorCoilDelayBetweenStepsMicros;
		}
	}

	if (isMotorOn && (motorCoilTurnOffMicros > 0)) {
		if (now - motorOnMicros >= motorCoilTurnOffMicros) {
			setState(motorStates[0]);
			isMotorOn = false;
		}
	}
}
