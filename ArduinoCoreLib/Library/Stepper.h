#define Stepper_h
#ifndef Stepper_h

#include "DigitalIO.h"

class StepperMotor {
public:
	virtual bool step(const bool moveForward) = 0; // true on success, false on error
	virtual void stop() = 0;
};

class StepperMosfetHBridge : public StepperMotor {
	signed char currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
public:

	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual bool step(const bool moveForward);
	virtual void stop();
};

class StepperMosfetHBridgeWithLengthControl : public StepperMosfetHBridge {
public:
	unsigned int remainingSteps;
	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual bool step(const bool moveForward);
	virtual void stop();
};

class StepperMosfetHBridgeWithButtons : public StepperMosfetHBridgeWithLengthControl {
public:
	long currentStep;
	DigitalInputPin *startPositionButton;
	DigitalInputPin *endPositionButton;

	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin,
			DigitalInputPin *startButton,
			DigitalInputPin *endButton);
	virtual bool step(const bool moveForward);
};

class SteperSpeedControl {
public:
	StepperMotor * motor;
	uint8_t movementMode;
	unsigned long delayBetweenStepsMicros;
	unsigned long stepAtMicros;

	void initialize(StepperMotor * motor);
	void update();
	void move(bool foreward);
	void stop();
};

////////

class StepperAxis {
public:
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
	const unsigned long timeToStopMicros = 5000;

	// length control
	unsigned int remainingSteps;

	void initialize(
			StepperMotor *motor,
			DigitalInputPin *startButton,
			DigitalInputPin *endButton);
	void update();

	void gotoStep(long step);
	void rotate(bool foreward);
	void stop();

	inline bool isMoving() {
		return movementMode != StepperAxis::Idle;
	}
};

////////

class SteppingMotorControl : public Updateable {
private:
public:
	uint8_t movementMode; // 0 - goto step; 1 - move forward; 2 - move backward

	long targetStep;

	long step;

	long stepsMadeSoFar;

	unsigned long motorCoilOnMicros;

	unsigned long delayBetweenStepsMicros;

	SteppingMotor *motor;
public:

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	virtual void update();

	void gotoStep(const long step);

	void rotate(const bool forward);

	void stop();

	void resetStepTo(const long step);

	bool isMoving();

	inline long getStep(void) {
		return step;
	}

	inline long getStepsMadeSoFar(void) {
		return stepsMadeSoFar;
	}

	inline void resetStepsMadeSoFar(void) {
		stepsMadeSoFar = 0;
	}

	inline void setDelayBetweenStepsMicros(unsigned long delayBetweenStepsMicros) {
		this->delayBetweenStepsMicros = delayBetweenStepsMicros;
	}

	inline unsigned long getDelayBetweenStepsMicros(void) {
		return delayBetweenStepsMicros;
	}
};

#define SteppingMotorControlIdle 0
#define SteppingMotorControlError 1
#define SteppingMotorControlDetermineAvailableSteps 10
#define SteppingMotorControlInitializeToStartingPosition 11

class SteppingMotorControlWithButtons : public Updateable {
private:
public:
	SteppingMotorControl motorControl;

	DigitalInputPin *startPositionButton;

	DigitalInputPin *endPositionButton;

	uint8_t mode;

	uint8_t modeState;

	long minStep;

	long maxStep;

	void doInitializeToStartingPosition();

	void doDetermineAvailableSteps();

public:
	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor,
			DigitalInputPin *startPositionButtonPin,
			DigitalInputPin *endPositionButtonPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	virtual void update();

	void initializeToStartingPosition();
	void determineAvailableSteps();

	void gotoStep(const long step);

	void rotate(const bool forward);

	void stop();

	bool isMoving();

	inline long getStep(void) {
		return motorControl.getStep();
	}

	inline long getMinStep(void) {
		return minStep;
	}

	inline long getMaxStep(void) {
		return maxStep;
	}

	inline bool isOk(void) {
		return mode != SteppingMotorControlError;
	}

	inline long getStepsMadeSoFar(void) {
		return motorControl.getStepsMadeSoFar();
	}

	inline void resetStepsMadeSoFar(void) {
		motorControl.resetStepsMadeSoFar();
	}

	inline void setDelayBetweenStepsMicros(unsigned long motorCoilDelayBetweenStepsMicros) {
		motorControl.setDelayBetweenStepsMicros(motorCoilDelayBetweenStepsMicros);
	}

	inline unsigned long getDelayBetweenStepsMicros(void) {
		return motorControl.getDelayBetweenStepsMicros();
	}
};

#endif
