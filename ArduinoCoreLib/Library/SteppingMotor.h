#ifndef SteppingMotor_h
#define SteppingMotor_h

#include "DigitalIO.h"
#include "TicksPerSecond.h"

class SteppingMotor {
public:
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
	signed char currentState;

	DigitalOutputPin *out11pin;
	DigitalOutputPin *out12pin;
	DigitalOutputPin *out21pin;
	DigitalOutputPin *out22pin;

	void setState(const uint8_t state);
	unsigned long motorCoilOnMicros;
	uint8_t mode;
public:
	TicksPerSecond tps;
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

class SteppingMotorControl {
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
	void update();

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
