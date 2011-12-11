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

public:
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

/////////

/**
 * StepperAxis uses MICRO meters for expressing lengths when working with stepper motors.
 */
class StepperMotorAxis {
private:
	void doDetermineAvailableSteps();

	enum AxisModes {
		Idle = 0,
		Error = 1,
		Waitings = 2,
		InitializePosition = 3,
		DetermineAvailableSteps = 10,
	};
	AxisModes mode;
	uint8_t modeState;

	unsigned int axisResolution; // in steps per decimeter
	int homePositionMM; // in absolute coords - coordinates are in milimeters
	unsigned long delayBetweenStepsAtMaxSpeedMicros;

public:
	StepperMotorControlWithButtons motorControl;

	void initialize(StepperMotor *motor,
			DigitalInputPin *startButton,
			DigitalInputPin *endButton);
	void update();

	void determineAvailableSteps(void);

	void moveToPositionMicroM(long absolutePositionMicroM, unsigned long delayBetweenStepsMicros);

	long getAbsolutePositionMicroM();

	inline void stop(void) {
			motorControl.stop();
	}

	inline void moveToPositionMicroMFast(long absolutePositionMicroM) {
		moveToPositionMicroM(absolutePositionMicroM, delayBetweenStepsAtMaxSpeedMicros);
	}

	inline void moveToHomePosition() {
		moveToPositionMicroMFast(((long) homePositionMM) * 1000L);
	}

	inline void setDelayBetweenStepsAtMaxSpeedMicros(unsigned long delayBetweenStepsAtMaxSpeedMicros) {
		this->delayBetweenStepsAtMaxSpeedMicros = delayBetweenStepsAtMaxSpeedMicros;
	}

	inline unsigned long getDelayBetweenStepsAtMaxSpeedMicros() {
		return delayBetweenStepsAtMaxSpeedMicros;
	}

	inline void setAxisResolution(unsigned int axisResolution) {
		this->axisResolution = axisResolution;
	}

	inline unsigned int getAxisResolution() {
		return axisResolution;
	}

	inline void setHomePositionMM(int homePositionMM) {
		this->homePositionMM = homePositionMM;
	}

	inline int getHomePositionMM() {
		return homePositionMM;
	}
};

#endif
