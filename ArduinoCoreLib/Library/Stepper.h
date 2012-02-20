#ifndef Stepper_h
#define Stepper_h

#include "HardwareSerial.h"
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
	int8_t currentState;

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

	void moveTo(long step, unsigned long timeToMakeTheMoveMicors);
	void gotoStep(long step);
	void rotate(bool foreward);
	void stop();

	inline bool isMoving() {
		return movementMode != StepperMotorControlWithButtons::Idle;
	}

	inline long getStep(void) {
		return currentStep;
	}
	inline void resetStep(long step) {
		currentStep = step;
	}

	inline void setDelayBetweenStepsMicros(unsigned long delayBetweenStepsMicros) {
		this->delayBetweenStepsMicros = delayBetweenStepsMicros;
	}

	inline unsigned long getDelayBetweenStepsMicros(void) {
		return delayBetweenStepsMicros;
	}

	void debugPrint();
};

/////////

/**
 * StepperAxis uses MICRO meters for expressing lengths when working with stepper motors.
 */
class StepperMotorAxis {
private:
	void doInitializePosition();
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

	void initializePosition();

	void moveToPositionMicroM(long absolutePositionMicroM, unsigned long timeToMakeTheMoveMicors);

	long getAbsolutePositionMicroM();

	void stop(void);

	void moveToPositionMicroMFast(long absolutePositionMicroM);

	inline void moveToHomePosition() {
		moveToPositionMicroMFast(((long) homePositionMM) * 1000L);
	}

	inline void setDelayBetweenStepsAtMaxSpeedMicros(unsigned long delayBetweenStepsAtMaxSpeedMicros) {
		this->delayBetweenStepsAtMaxSpeedMicros = delayBetweenStepsAtMaxSpeedMicros;
	}

	inline unsigned long getDelayBetweenStepsAtMaxSpeedMicros() {
		return delayBetweenStepsAtMaxSpeedMicros;
	}

	/**
	 * The resolution is specified in motor steps per DECImeter
	 */
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

	inline bool isIdle() {
		return (mode == StepperMotorAxis::Idle) && (!motorControl.isMoving());
	}

	/**
	 * Speed is in mm/min
	 * Delay between steps is in microseconds
	 * Uses the parameter axisResolution
	 */
	inline unsigned int delayBetweenStepsToSpeed(unsigned long delayBetweenStepsMicros) {
		return (unsigned int) (((60000000UL / getAxisResolution()) * 100UL) / delayBetweenStepsMicros);
	}

	inline unsigned long speedToDelayBetweenSteps(unsigned int speed) {
		return (((60000000UL / getAxisResolution()) * 100UL) / speed);
	}

	inline void setSpeed(unsigned int speed) {
		motorControl.setDelayBetweenStepsMicros(speedToDelayBetweenSteps(speed));
	}

	inline unsigned int getSpeed() {
		return delayBetweenStepsToSpeed(motorControl.getDelayBetweenStepsMicros());
	}

	void debugPrint();
};

#endif