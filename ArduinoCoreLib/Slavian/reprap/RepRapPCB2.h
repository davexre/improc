#ifndef RepRapPCB_h
#define RepRapPCB_h

#include "Arduino.h"
#include "utils.h"
#include "DigitalIO.h"
#include "Stepper.h"

class RepRapPCB2 {
private:
	static const int shiftRegisterInputPinCP = 8;
	static const int shiftRegisterInputPinPE = 9;
	static const int shiftRegisterInputPinQ7 = 10;

	static const int shiftRegisterOutputPinDS = 11;
	static const int shiftRegisterOutputPinSH = 12;
	static const int shiftRegisterOutputPinST = 13;

	DigitalOutputArduinoPin diShiftRegisterOutputPinSH;
	DigitalOutputArduinoPin diShiftRegisterOutputPinST;
	DigitalOutputArduinoPin diShiftRegisterOutputPinDS;

	DigitalOutputArduinoPin diShiftRegisterInputPinPE;
	DigitalOutputArduinoPin diShiftRegisterInputPinCP;
	DigitalInputArduinoPin diShiftRegisterInputPinQ7;

	enum RepRapPcbMode {
		Idle = 0,
		InitializePosition = 2,
		InitializePositionXY = 3,
	};
	RepRapPcbMode mode;
	uint8_t modeState;

	void setMode(RepRapPcbMode mode);
	void doInitializePosition();
	void doInitializePositionXY();
public:
	DigitalOutputShiftRegister_74HC595 extenderOutput;
	DigitalInputShiftRegister_74HC166 extenderInput;

	StepperMotorMosfetHBridge motorX;
	StepperMotorMosfetHBridge motorY;
	StepperMotorMosfetHBridge motorZ;
	StepperMotorMosfetHBridge motorE;

	StepperMotorAxis axisX;
	StepperMotorAxis axisY;
	StepperMotorAxis axisZ;
	StepperMotorAxis axisE;

	DigitalOutputPin *fan;

	void initialize();
	void update();

	bool isIdle();
	inline void initializePosition() {
		setMode(RepRapPCB2::InitializePosition);
	}
	inline void initializePositionXY() {
		setMode(RepRapPCB2::InitializePositionXY);
	}

	/*
	 * xPositionMicroM, yPositionMicroM - target position in micro meters
	 * speed - speed in mm/min
	 */
	void moveToXY(long xPositionMicroM, long yPositionMicroM, unsigned int speed);

	void moveToHomePosition();

	void stop();

	void debugPrint();
};

#endif
