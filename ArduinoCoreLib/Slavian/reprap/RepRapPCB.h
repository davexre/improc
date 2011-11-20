#ifndef RepRapPCB_h
#define RepRapPCB_h

#include "utils.h"
#include "DigitalIO.h"
#include "SteppingMotor.h"
#include "StepperAxis.h"

class RepRapPCB {
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
