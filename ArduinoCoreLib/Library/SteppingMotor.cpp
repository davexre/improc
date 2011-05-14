#include "Arduino.h"
#include "SteppingMotor.h"

void SteppingMotor::initialize(uint8_t out11pin, uint8_t out12pin, uint8_t out21pin, uint8_t out22pin) {
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

static const uint8_t motorStates[][4] = {
		{ LOW,  LOW,  LOW,  LOW  },
		{ LOW,  HIGH, LOW,  LOW  },
		{ LOW,  LOW,  LOW,  HIGH },
		{ HIGH, HIGH, LOW,  LOW  },
		{ LOW,  LOW,  HIGH, HIGH },

};

void SteppingMotor::setState(const uint8_t *state) {
	digitalWrite(out11pin, *(state++));
	digitalWrite(out12pin, *(state++));
	digitalWrite(out21pin, *(state++));
	digitalWrite(out22pin, *(state++));
}

void SteppingMotor::stop() {
	setState(motorStates[0]);
	isMotorOn = false;
	movementMode = 0;
	targetStep = step;
}

void SteppingMotor::gotoStep(long step) {
	movementMode = 0;
	targetStep = step;
}

void SteppingMotor::rotate(boolean forward) {
	movementMode = forward ? 1 : 2;
}

void SteppingMotor::resetStepTo(long step) {
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
