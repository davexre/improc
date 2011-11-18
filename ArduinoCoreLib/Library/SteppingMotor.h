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
	signed char currentState;

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
private:
	signed char currentState;
	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;
	uint8_t steppingMotorMode;
	uint8_t stepSwitchingMode;
	uint8_t mode;
	unsigned long motorCoilOnMicros;

	void setState(const uint8_t state);
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

	unsigned long motorCoilTurnOffMicros;

	void initialize(
			SteppingMotorMode steppingMotorMode,
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
};

class SteppingMotorControl {
private:
	static const unsigned long MinDelayBetweenStepsMicros = 1000UL;

	enum MovementMode {
		Idle = 1,
		ContinuousForward = 2,
		ContinuousBackward = 3,
		GotoStepWithDelayBetweenSteps = 4,
		GotoStepWithTimePeriod = 5,
		GotoStepInFixedTimeVariableSpeed = 6,
		Stopping = 7
	};
	/**
	 * 0 - goto step;
	 * 1 - move forward;
	 * 2 - move backward
	 */
	MovementMode movementMode;

	long targetStep;

	long step;

	unsigned long stepAtMicros;
	unsigned long timeMicros;

	unsigned long delayBetweenStepsMicros;
	long stepsMadeSoFar;

	SteppingMotor *motor;
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

class SteppingMotorControlWithButtons {
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
	void update();

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
