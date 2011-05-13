#include "SteppingMotor.h"

void SteppingMotor::initialize(uint8_t out11pin, uint8_t out12pin, uint8_t out21pin, uint8_t out22pin) {
	this->out11pin = out11pin;
	this->out12pin = out12pin;
	this->out21pin = out21pin;
	this->out22pin = out22pin;

	pinMode(out11pin, OUTPUT);
	digitalWrite(out11pin, LOW);

	pinMode(out12pin, OUTPUT);
	digitalWrite(out12pin, LOW);

	pinMode(out21pin, OUTPUT);
	digitalWrite(out21pin, LOW);

	pinMode(out22pin, OUTPUT);
	digitalWrite(out22pin, LOW);

	currentState = 0;
	step = 0;
}

static const uint8_t motorStates[][4] = {
		{ LOW,  LOW,  LOW,  LOW  },
		{ HIGH, LOW,  LOW,  LOW  },
		{ LOW,  LOW,  HIGH, LOW  },
		{ LOW,  HIGH, LOW,  LOW  },
		{ LOW,  LOW,  LOW,  HIGH },

};

void SteppingMotor::setState(const uint8_t *state) {
	digitalWrite(out11pin, state[0]);
	digitalWrite(out12pin, state[1]);
	digitalWrite(out21pin, state[2]);
	digitalWrite(out22pin, state[3]);
}

void SteppingMotor::previousStep() {
	step--;
	currentState--;
	if (currentState <= 0)
		currentState = size(motorStates) - 1;
	setState(motorStates[currentState]);
}

void SteppingMotor::nextStep() {
	step++;
	currentState++;
	if (currentState >= size(motorStates))
		currentState = 1;
	setState(motorStates[currentState]);
}

void SteppingMotor::stop() {
	setState(motorStates[0]);
}

void SteppingMotor::update() {
}
