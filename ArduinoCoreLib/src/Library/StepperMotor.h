#ifndef StepperMotor_h
#define StepperMotor_h

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

	void setState(const uint8_t state) {
		out11pin->setState(state & 0b01000);
		out12pin->setState(state & 0b00100);
		out21pin->setState(state & 0b00010);
		out22pin->setState(state & 0b00001);
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

	/**
	 * Half power consumption, lower torque
	 */
	static const uint8_t motorStatesBA6845FS_HalfPower[];

	/**
	 * Full power consumption & torque
	 */
	static const uint8_t motorStatesBA6845FS_FullPower[];

	/**
	 * Higher (double) precision, variable power consumption & torque
	 */
	static const uint8_t motorStatesBA6845FS_DoublePrecision[];
public:
	void initialize(
			StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
		this->out11pin = out11pin;
		this->out12pin = out12pin;
		this->out21pin = out21pin;
		this->out22pin = out22pin;
		currentState = motorMode;
		stop();
	}

	virtual void step(const bool moveForward) {
		uint8_t mode = currentState & 0b00000011;
		int8_t index = currentState >> 2;
		int8_t maxSteps = mode == StepperMotor::DoublePrecision ? 8 : 4;
		if (moveForward) {
			index++;
			if (index >= maxSteps)
				index = 1;
		} else {
			index--;
			if (index < 0)
				index = maxSteps - 1;
		}
		currentState = (index << 2) | mode;
		uint8_t state;
		switch (mode) {
		case DoublePrecision:
			state = motorStatesBA6845FS_DoublePrecision[index];
			break;
		case FullPower:
			state = motorStatesBA6845FS_FullPower[index];
			break;
		case HalfPower:
		default:
			state = motorStatesBA6845FS_HalfPower[index];
			break;
		}
		setState(state);
	}

	virtual void stop() {
		setState(0);
	}

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

	void setState(const uint8_t state) {
		out11pin->setState(state & 0b01000);
		out12pin->setState(state & 0b00100);
		out21pin->setState(state & 0b00010);
		out22pin->setState(state & 0b00001);
	}

	/**
	 * Full power consumption & torque
	 */
	static const uint8_t motorStatesMosfetHBridge_FullPower[] PROGMEM;

	/**
	 * Half power consumption, lower torque
	 */
	static const uint8_t motorStatesMosfetHBridge_HalfPower[] PROGMEM;

	/**
	 * Higher (double) precision, variable power consumption & torque
	 */
	static const uint8_t motorStatesMosfetHBridge_DoublePrecision[] PROGMEM;

public:

	void initialize(
			StepperMotor::SteppingMotorMode motorMode,
			DigitalOutputPin *out11pin,
			DigitalOutputPin *out12pin,
			DigitalOutputPin *out21pin,
			DigitalOutputPin *out22pin) {
		this->out11pin = out11pin;
		this->out12pin = out12pin;
		this->out21pin = out21pin;
		this->out22pin = out22pin;
		currentState = motorMode;
		stop();
	}

	virtual void step(const bool moveForward) {
		uint8_t mode = currentState & 0b00000011;
		int8_t index = currentState >> 2;
		int8_t maxSteps = mode == StepperMotor::DoublePrecision ? 8 : 4;
		if (moveForward) {
			index++;
			if (index >= maxSteps)
				index = 0;
		} else {
			index--;
			if (index < 0)
				index = maxSteps - 1;
		}
		currentState = (index << 2) | mode;
		const uint8_t *state;
		switch (mode) {
		case DoublePrecision:
			state = &motorStatesMosfetHBridge_DoublePrecision[index];
			break;
		case FullPower:
			state = &motorStatesMosfetHBridge_FullPower[index];
			break;
		case HalfPower:
		default:
			state = &motorStatesMosfetHBridge_HalfPower[index];
			break;
		}
		setState(pgm_read_byte(state));
	}

	virtual void stop() {
		setState(0);
	}

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
			DigitalInputPin *endButton) {
		this->motor = motor;
		this->startButton = startButton;
		this->endButton = endButton;
		currentStep = 0;
		movementMode = StepperMotorControlWithButtons::Idle;
		delayBetweenStepsMicros = 2000UL;
	}

	void update() {
		bool moveForeward;
		switch (movementMode) {
		case Idle:
			return;
		case Backward:
		case GotoStartButton:
			moveForeward = false;
			break;
		// case Foreward:
		// case GotoEndButton:
		default:
			moveForeward = true;
			break;
		}

		unsigned long dT = micros() - lastTimestampMicros;
		// Stopping & End buttons check
		if ((movementMode == StepperMotorControlWithButtons::Stopping) ||
			(moveForeward && !endButton->getState()) ||
			(!moveForeward && !startButton->getState())) {
			if (dT >= timeToStopMicros) {
				motor->stop();
				movementMode = StepperMotorControlWithButtons::Idle;
	//			if (!moveForeward)
	//				currentStep = 0;	// TODO: Is this really necessary?
			}
			return;
		}

		// Speed control
		if (dT < delayBetweenStepsMicros) {
			if (dT >= timeToStopMicros) {
				motor->stop();
			}
			return;
		}

		if ((movementMode == StepperMotorControlWithButtons::Foreward) ||
			(movementMode == StepperMotorControlWithButtons::Backward)) {
			if (remainingSteps == 0) {
				movementMode = StepperMotorControlWithButtons::Stopping;
				return;
			}
			remainingSteps--;
		}

		if (moveForeward) {
			currentStep++;
		} else {
			currentStep--;
		}
		motor->step(moveForeward);
		lastTimestampMicros += delayBetweenStepsMicros; // should increase, not set to now!
	}

	void moveTo(long step, unsigned long timeToMakeTheMoveMicors) {
		step -= currentStep;
		if (step >= 0) {
			movementMode = StepperMotorControlWithButtons::Foreward;
			remainingSteps = step;
		} else {
			movementMode = StepperMotorControlWithButtons::Backward;
			remainingSteps = -step;
		}
		delayBetweenStepsMicros = remainingSteps;
		if (remainingSteps < 3) {
			delayBetweenStepsMicros++;
		}
		delayBetweenStepsMicros = timeToMakeTheMoveMicors / delayBetweenStepsMicros;
		lastTimestampMicros = micros() - delayBetweenStepsMicros;
	}

	void gotoStep(long step) {
		step -= currentStep;
		if (step >= 0) {
			movementMode = StepperMotorControlWithButtons::Foreward;
			remainingSteps = step;
		} else {
			movementMode = StepperMotorControlWithButtons::Backward;
			remainingSteps = -step;
		}
		lastTimestampMicros = micros() - delayBetweenStepsMicros;
	}

	void rotate(bool foreward) {
		movementMode = foreward ? StepperMotorControlWithButtons::GotoEndButton : StepperMotorControlWithButtons::GotoStartButton;
		lastTimestampMicros = micros() - delayBetweenStepsMicros;
	}

	void stop() {
		if (movementMode != StepperMotorControlWithButtons::Idle) {
			movementMode = StepperMotorControlWithButtons::Stopping;
		}
	}

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

	static const char pgm_Up[] PROGMEM;
	static const char pgm_Down[] PROGMEM;

	void debugPrint() {
		Serial.pgm_print(PSTR("isMoving:       ")); Serial.println(isMoving() ? 'T':'F');
		Serial.pgm_print(PSTR("remaining steps:")); Serial.println(remainingSteps);
		Serial.pgm_print(PSTR("cur step:       ")); Serial.println(getStep());
		Serial.println();
		Serial.pgm_print(PSTR("start button:   ")); Serial.pgm_println(startButton->getState() ? pgm_Up : pgm_Down);
		Serial.pgm_print(PSTR("end button:     ")); Serial.pgm_println(endButton->getState() ? pgm_Up : pgm_Down);
		Serial.println();
		Serial.pgm_print(PSTR("movement mode:  ")); Serial.println((int)movementMode);
		Serial.pgm_print(PSTR("lastTimestMicro:")); Serial.println(lastTimestampMicros);
		Serial.pgm_print(PSTR("delay b/n steps:")); Serial.println(delayBetweenStepsMicros);
	}
};

/////////

/**
 * StepperAxis uses MICRO meters for expressing lengths when working with stepper motors.
 */
class StepperMotorAxis {
private:
	void doInitializePosition() {
		switch (modeState) {
		case 0:
			motorControl.setDelayBetweenStepsMicros(getDelayBetweenStepsAtMaxSpeedMicros());
			motorControl.rotate(false);
			modeState = 1;
			break;
		case 1:
			if (!motorControl.isMoving()) {
				motorControl.resetStep(0);
				mode = StepperMotorAxis::Idle;
			}
			break;
		}
	}

	void doDetermineAvailableSteps() {
		switch (modeState) {
		case 0:
			motorControl.setDelayBetweenStepsMicros(delayBetweenStepsAtMaxSpeedMicros);
			motorControl.rotate(false);
			modeState = 1;
			break;
		case 1:
			if (!motorControl.isMoving()) {
				motorControl.rotate(true);
				modeState = 2;
			}
			break;
		case 2:
			if (!motorControl.isMoving()) {
				mode = StepperMotorAxis::Idle;
			}
			break;
		}
	}

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
			DigitalInputPin *endButton) {
		motorControl.initialize(motor, startButton, endButton);
		homePositionMM = -32767;
		delayBetweenStepsAtMaxSpeedMicros = 2000;
		axisResolution = 1000;
		mode = StepperMotorAxis::Idle;
	}

	void update() {
		motorControl.update();
		switch (mode) {
		case InitializePosition:
			doInitializePosition();
			break;
		case DetermineAvailableSteps:
			doDetermineAvailableSteps();
			break;
		default:
			modeState = StepperMotorAxis::Idle;
			break;
		}
	}

	void determineAvailableSteps(void) {
		mode = StepperMotorAxis::DetermineAvailableSteps;
		modeState = 0;
	}

	void initializePosition() {
		mode = StepperMotorAxis::InitializePosition;
		modeState = 0;
	}

	void moveToPositionMicroM(long absolutePositionMicroM, unsigned long timeToMakeTheMoveMicors) {
		motorControl.moveTo(((absolutePositionMicroM / 100L) * axisResolution) / 1000L, timeToMakeTheMoveMicors);
	}

	long getAbsolutePositionMicroM() {
		return ((motorControl.getStep() * 1000L) / axisResolution) * 100L;
	}

	void stop(void) {
		motorControl.stop();
		mode = StepperMotorAxis::Idle;
	}

	void moveToPositionMicroMFast(long absolutePositionMicroM) {
		motorControl.setDelayBetweenStepsMicros(getDelayBetweenStepsAtMaxSpeedMicros());
		motorControl.gotoStep(((absolutePositionMicroM / 100L) * axisResolution) / 1000L);
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

	void debugPrint() {
		Serial.pgm_println(PSTR("AXIS state"));
		Serial.pgm_print(PSTR("AXIS mode:      ")); Serial.print((int)mode); Serial.print('/'); Serial.println((int)modeState);
		Serial.pgm_print(PSTR("abs position:   ")); Serial.println(getAbsolutePositionMicroM());
		Serial.pgm_print(PSTR("speed (Q):      ")); Serial.println(getSpeed());
		Serial.println();
		motorControl.debugPrint();
		Serial.pgm_print(PSTR("delay b/n steps@max speed:")); Serial.println(getDelayBetweenStepsAtMaxSpeedMicros());
		Serial.pgm_print(PSTR("axis resolution:")); Serial.println(getAxisResolution());
		Serial.pgm_print(PSTR("home position:  ")); Serial.println(getHomePositionMM());
		Serial.println();
	}
};

#endif
