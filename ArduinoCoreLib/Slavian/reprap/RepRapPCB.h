#ifndef RepRapPCB_h
#define RepRapPCB_h

#include "utils.h"
#include "DigitalIO.h"
#include "SteppingMotor.h"
#include "StepperAxis.h"

class RepRapPCB {
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
public:
	DigitalOutputShiftRegister_74HC595 extenderOutput;
	DigitalInputShiftRegister_74HC166 extenderInput;

	SteppingMotor_MosfetHBridge motorX;
	SteppingMotor_MosfetHBridge motorY;
	SteppingMotor_MosfetHBridge motorZ;
	SteppingMotor_MosfetHBridge motorE;

	StepperAxis axisX;
	StepperAxis axisY;
	StepperAxis axisZ;
	StepperAxis axisE;

	DigitalOutputPin *fan;

	void initialize();
	void update();
};

#endif
