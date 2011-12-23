#include "RepRapPCB2.h"

void RepRapPCB2::initialize() {
	diShiftRegisterOutputPinSH.initialize(shiftRegisterOutputPinSH);
	diShiftRegisterOutputPinST.initialize(shiftRegisterOutputPinST);
	diShiftRegisterOutputPinDS.initialize(shiftRegisterOutputPinDS);
	extenderOutput.initialize(17, DigitalOutputShiftRegister_74HC595::BeforeWriteZeroOnlyModifiedOutputs,
			&diShiftRegisterOutputPinSH,
			&diShiftRegisterOutputPinST,
			&diShiftRegisterOutputPinDS);
	diShiftRegisterInputPinPE.initialize(shiftRegisterInputPinPE);
	diShiftRegisterInputPinCP.initialize(shiftRegisterInputPinCP);
	diShiftRegisterInputPinQ7.initialize(shiftRegisterInputPinQ7, false);
	extenderInput.initialize(9,
			&diShiftRegisterInputPinPE,
			&diShiftRegisterInputPinCP,
			&diShiftRegisterInputPinQ7);

	motorX.initialize(
			StepperMotor::HalfPower,
			extenderOutput.createPinHandler(0),
			extenderOutput.createPinHandler(1),
			extenderOutput.createPinHandler(2),
			extenderOutput.createPinHandler(3));
	motorY.initialize(
			StepperMotor::HalfPower,
			extenderOutput.createPinHandler(4),
			extenderOutput.createPinHandler(5),
			extenderOutput.createPinHandler(6),
			extenderOutput.createPinHandler(7));
	motorZ.initialize(
			StepperMotor::HalfPower,
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));
	motorE.initialize(
			StepperMotor::HalfPower,
			extenderOutput.createPinHandler(12),
			extenderOutput.createPinHandler(13),
			extenderOutput.createPinHandler(14),
			extenderOutput.createPinHandler(15));

	fan = extenderOutput.createPinHandler(16);

	axisX.initialize(&motorX, extenderInput.createPinHandler(8), extenderInput.createPinHandler(0));
	axisY.initialize(&motorY, extenderInput.createPinHandler(1), extenderInput.createPinHandler(2));
	axisZ.initialize(&motorZ, extenderInput.createPinHandler(4), extenderInput.createPinHandler(3));
	axisE.initialize(&motorE, extenderInput.createPinHandler(5), extenderInput.createPinHandler(6));

	axisX.setDelayBetweenStepsAtMaxSpeedMicros(1500);
	axisY.setDelayBetweenStepsAtMaxSpeedMicros(1500);
	axisZ.setDelayBetweenStepsAtMaxSpeedMicros(2500);
	axisE.setDelayBetweenStepsAtMaxSpeedMicros(2000);

	axisX.motorControl.setDelayBetweenStepsMicros(axisX.getDelayBetweenStepsAtMaxSpeedMicros());
	axisY.motorControl.setDelayBetweenStepsMicros(axisY.getDelayBetweenStepsAtMaxSpeedMicros());
	axisZ.motorControl.setDelayBetweenStepsMicros(axisZ.getDelayBetweenStepsAtMaxSpeedMicros());
	axisE.motorControl.setDelayBetweenStepsMicros(axisE.getDelayBetweenStepsAtMaxSpeedMicros());

	axisX.setAxisResolution(1000);
	axisY.setAxisResolution(1000);
	axisZ.setAxisResolution(1000);
	axisE.setAxisResolution(1000);

	axisX.setHomePositionMM(1000);
	axisY.setHomePositionMM(1000);
	axisZ.setHomePositionMM(1000);
	axisE.setHomePositionMM(1000);
}

void RepRapPCB2::update() {
	extenderInput.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();

	extenderOutput.update();
}
