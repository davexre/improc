#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "DigitalIO.h"

class SteppingMotor {
public:
	enum SteppingMotorMode {
		HalfPower = 0,
		FullPower = 1,
		DoublePrecision = 2
	};
	virtual void step(const bool moveForward) = 0;
	virtual void stop() = 0;
};

class SteppingMotor_BA6845FS : public SteppingMotor {
	int8_t currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
	unsigned long motorCoilOnMicros;
	bool isMotorCoilOn;
public:
	unsigned long motorCoilTurnOffMicros;

	void initialize(
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();
};

class SteppingMotor_MosfetHBridge : public SteppingMotor {
public:
	enum StepSwitchingMode {
		/**
		 * The stepping is performed by turning off HBridge in a
		 * separate loop (invokation of update method)
		 * This is to be used with:
		 * + new DigitalOutputArduinoPin(?)
		 * + DigitalOutputShiftRegister_74HC595.initialize(?, writeOutputMode, ?,?,?,?);
		 *     where writeOutputMode is:
		 *       DigitalOutputShiftRegister_74HC595::WriteOnEveryUpdate
		 *       DigitalOutputShiftRegister_74HC595::WriteOnlyIfModified
		 */
		TurnOffInSeparateCycle = 1,

		/**
		 * The HBridges are turned off before setting the new state
		 * This is to be used ONLY with:
		 * + new DigitalOutputArduinoPin(?)
		 */
		TurnOffInSameCycle = 2,

		/**
		 * The HBridges will NOT be turned off prior to setting the new
		 * output state. This MIGHT lead to a situation where all MOSFETS
		 * will be opened and an avalanche current will destroy them.
		 * This is to be used ONLY with:
		 * + DigitalOutputShiftRegister_74HC595.initialize(?, writeOutputMode, ?,?,?,?);
		 *     where writeOutputMode is:
		 *       DigitalOutputShiftRegister_74HC595::BeforeWriteZeroAllOutputs
		 *       DigitalOutputShiftRegister_74HC595::BeforeWriteZeroOnlyModifiedOutputs
		 */
		DoNotTurnOff = 3
	};

private:
	int8_t currentState;
	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;
	uint8_t mode;
	unsigned long motorCoilOnMicros;
	SteppingMotor::SteppingMotorMode steppingMotorMode;

	StepSwitchingMode stepSwitchingMode;
	unsigned long motorCoilTurnOffMicros;

	void setState(const uint8_t state);

public:
	void initialize(
			SteppingMotor::SteppingMotorMode steppingMotorMode,
			StepSwitchingMode stepSwitchingMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin);
	virtual void step(const bool moveForward);
	virtual void stop();

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	inline void setMotorCoilTurnOffMicros(unsigned long motorCoilTurnOffMicros) {
		this->motorCoilTurnOffMicros = motorCoilTurnOffMicros;
	}
	inline unsigned long getMotorCoilTurnOffMicros() {
		return motorCoilTurnOffMicros;
	}
};

class SteppingMotorControl {
private:
	int minDelayBetweenStepsMicros;

	enum MovementMode {
		Idle = 1,
		ContinuousForward = 2,
		ContinuousBackward = 3,
		GotoStepWithDelayBetweenSteps = 4,
		GotoStepWithTimePeriod = 5,
		GotoStepInFixedTimeVariableSpeed = 6,
		Stopping = 7
	};
	MovementMode movementMode;

	long targetStep;

	long currentStep;

	unsigned long stepAtMicros;
	unsigned long timeMicros;

	unsigned long delayBetweenStepsMicros;
	long stepsMadeSoFar;

	SteppingMotor *motor;
protected:
	virtual bool step(bool forward); // true -> success, false -> will not step
public:
	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	void gotoStep(const long step);

	void gotoStepInMicros(const long step, const unsigned long timeToStepMicros);

	void gotoStepInFixedTimeVariableSpeed(const long step);

	void rotate(const bool forward);

	void stop();

	void resetStepTo(const long step);

	bool isMoving();

	inline long getStep(void) {
		return currentStep;
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

	inline void setMinDelayBetweenStepsMicros(int minDelayBetweenStepsMicros) {
		this->minDelayBetweenStepsMicros = minDelayBetweenStepsMicros;
	}
	inline int getMinDelayBetweenStepsMicros() {
		return minDelayBetweenStepsMicros;
	}
};

class SteppingMotorControlWithButtons : private SteppingMotorControl {
private:
	int maxStepsWithWrongButtonDown;

	enum MotorControlModes {
		Idle = 0,
		Error = 1,
		Waiting = 2,
		DetermineAvailableSteps = 10,
		InitializeToStartPosition = 11,
		InitializeToEndPosition = 12
	};

	MotorControlModes mode;

	uint8_t modeState;

	long minStep;

	long maxStep;

	void doInitializeToStartPosition();
	void doInitializeToEndPosition();

	void doDetermineAvailableSteps();
protected:
	virtual bool step(bool forward); // true -> success, false -> will not step
public:
	DigitalInputPin *startPositionButton;

	DigitalInputPin *endPositionButton;

	/**
	 * Initializes the class, sets ports (outXXpin) to output mode.
	 */
	void initialize(SteppingMotor *motor,
			DigitalInputPin *startPositionButtonPin,
			DigitalInputPin *endPositionButtonPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update();

	void initializeToStartPosition();
	void initializeToEndPosition();
	void determineAvailableSteps();

	void gotoStep(const long step);

	void rotate(const bool forward);

	void stop();

	bool isMoving();

	inline long getStep(void) {
		return SteppingMotorControl::getStep();
	}

	inline long getMinStep(void) {
		return minStep;
	}

	inline long getMaxStep(void) {
		return maxStep;
	}

	inline void setMaxStepsWithWrongButtonDown(int maxStepsWithWrongButtonDown) {
		this->maxStepsWithWrongButtonDown = maxStepsWithWrongButtonDown;
	}
	inline int getMaxStepsWithWrongButtonDown() {
		return maxStepsWithWrongButtonDown;
	}

	inline bool isOk(void) {
		return mode != SteppingMotorControlWithButtons::Error;
	}

	inline long getStepsMadeSoFar(void) {
		return SteppingMotorControl::getStepsMadeSoFar();
	}

	inline void resetStepsMadeSoFar(void) {
		SteppingMotorControl::resetStepsMadeSoFar();
	}

	inline void setDelayBetweenStepsMicros(unsigned long motorCoilDelayBetweenStepsMicros) {
		SteppingMotorControl::setDelayBetweenStepsMicros(motorCoilDelayBetweenStepsMicros);
	}

	inline unsigned long getDelayBetweenStepsMicros(void) {
		return SteppingMotorControl::getDelayBetweenStepsMicros();
	}

	inline void setMinDelayBetweenStepsMicros(unsigned long motorCoilMinDelayBetweenStepsMicros) {
		SteppingMotorControl::setMinDelayBetweenStepsMicros(motorCoilMinDelayBetweenStepsMicros);
	}
	inline unsigned long getMinDelayBetweenStepsMicros(void) {
		return SteppingMotorControl::getMinDelayBetweenStepsMicros();
	}
};

#endif
