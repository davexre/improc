#ifndef Stepper_h
#define Stepper_h

#include "DigitalIO.h"

class StepperMotor {
public:
	enum SteppingMotorMode {
		HalfPower = 0,
		FullPower = 1,
		DoublePrecision = 2
	};

	virtual void step(const bool moveForward) = 0; // true on success, false on error
	virtual void stop() = 0;
};

class StepperMotorBA6845FS : public StepperMotor {
	signed char currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
public:
	void initialize(
			StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	inline void setMotorMode(StepperMotor::SteppingMotorMode motorMode) {
		currentState = motorMode;
	}

	inline StepperMotor::SteppingMotorMode getMotorMode() {
		return (StepperMotor::SteppingMotorMode) (currentState & 0b011);
	}
};

class StepperMotorMosfetHBridge : public StepperMotor {
	uint8_t currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
public:

	void initialize(
			StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	inline void setMotorMode(StepperMotor::SteppingMotorMode motorMode) {
		currentState = motorMode;
	}

	inline StepperMotor::SteppingMotorMode getMotorMode() {
		return (StepperMotor::SteppingMotorMode) (currentState & 0b011);
	}
};

////////

class StepperMotorControlWithButtons {
	enum MovementMode {
		Idle = 0,
		Stopping = 1,
		Foreward = 2,
		Backward = 3,
		GotoEndButton = 4,
		GotoStartButton = 5
	};

	StepperMotor *motor;
	long currentStep;

	// end buttons
	DigitalInputPin *startButton;
	DigitalInputPin *endButton;

	// speed control
	MovementMode movementMode;
	unsigned long delayBetweenStepsMicros;
	unsigned long lastTimestampMicros;
	static const unsigned long timeToStopMicros = 5000;

	// length control
	unsigned int remainingSteps;
public:

	void initialize(
			StepperMotor *motor,
			DigitalInputPin *startButton,
			DigitalInputPin *endButton);
	void update();

	void gotoStep(long step);
	void rotate(bool foreward);
	void stop();

	inline bool isMoving() {
		return movementMode != StepperMotorControlWithButtons::Idle;
	}

	inline long getStep(void) {
		return currentStep;
	}

	inline void setDelayBetweenStepsMicros(unsigned long delayBetweenStepsMicros) {
		this->delayBetweenStepsMicros = delayBetweenStepsMicros;
	}

	inline unsigned long getDelayBetweenStepsMicros(void) {
		return delayBetweenStepsMicros;
	}
};

#endif
