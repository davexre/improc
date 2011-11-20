#ifndef StepperAxis_h
#define StepperAxis_h

#include "DigitalIO.h"
#include "Button.h"
#include "SteppingMotor.h"
#include "utils.h"

/**
 * StepperAxis uses MICRO meters for expressing lengths when working with stepper motors.
 */
class StepperAxis {
private:
	enum AxisModes {
		Idle = 0,
		Error = 1,
		Waitings = 2,
		InitializePosition = 3
	};
	AxisModes mode;
	uint8_t modeState;

	unsigned long timestamp;
	long axisSteps;
	long axisLengthInMicroM;
	long axisHomePositionMicroM;
	unsigned long delayBetweenStepsAtMaxSpeedMicros;
	bool useStartPositionToInitialize; // True -> init to start, False -> init to end position
public:
	static const signed long StepperAxisMicroMToMoveBeforeInitialize = 1000000;
	SteppingMotorControlWithButtons motorControl;

	/**
	 * Initializes the class.
	 */
	void initialize(SteppingMotor *motor,
			DigitalInputPin *startPositionButtonPin,
			DigitalInputPin *endPositionButtonPin);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);

	void setAxisSteps(long axisSteps);
	inline long getAxisSteps() { return axisSteps; }

	void setAxisLengthInMicroM(long axisLengthInMicroM);
	inline long getAxisLengthInMicroM() {
		return axisLengthInMicroM;
	}

	inline void setAxisHomePositionMicroM(long axisHomePositionMicroM) {
		this->axisHomePositionMicroM = axisHomePositionMicroM;
	}
	inline long getAxisHomePositionMicroM() {
		return axisHomePositionMicroM;
	}

	inline void setDelayBetweenStepsAtMaxSpeedMicros(unsigned long delayBetweenStepsAtMaxSpeedMicros) {
		this->delayBetweenStepsAtMaxSpeedMicros = delayBetweenStepsAtMaxSpeedMicros;
	}
	inline unsigned long getDelayBetweenStepsAtMaxSpeedMicros() {
		return delayBetweenStepsAtMaxSpeedMicros;
	}

	inline void setUseStartPositionToInitialize(bool useStartPositionToInitialize) {
		this->useStartPositionToInitialize = useStartPositionToInitialize;
	}
	inline bool getUseStartPositionToInitialize() {
		return useStartPositionToInitialize;
	}

	void determineAvailableSteps(void);

	inline void moveToHomePosition() {
		moveToPositionMicroMFast(axisHomePositionMicroM);
	}

	void moveToPositionMicroMFast(long absolutePositionMicroM);
	void moveToPositionMicroM(long absolutePositionMicroM, unsigned long timeToMoveMicros);
	long getAbsolutePositionMicroM();
	inline long getStepPosition() {
		motorControl.getStep();
	}

	void stop(void) {
		motorControl.stop();
	}

	inline bool isMoving() {
		return motorControl.isMoving();
	}

	inline bool isOk() {
		return motorControl.isOk();
	}

	bool isInitializePositionNeeded();
	void initializePosition();
};

#endif
