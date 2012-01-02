#include "RepRapPCB2.h"

#define XAxisLengthInMicroM 323800UL
#define YAxisLengthInMicroM 303900UL
#define ZAxisLengthInMicroM 168200UL

#define XAxisLengthInSteps 4689
#define YAxisLengthInSteps 4103
#define ZAxisLengthInSteps 6488

#define XAxisPrecissionInSteps 4
#define YAxisPrecissionInSteps 10
#define ZAxisPrecissionInSteps 16

#define XAxisResolutionInStepsPerDecimeter (XAxisLengthInSteps * 1000UL / (XAxisLengthInMicroM / 100UL))
#define YAxisResolutionInStepsPerDecimeter (YAxisLengthInSteps * 1000UL / (YAxisLengthInMicroM / 100UL))
#define ZAxisResolutionInStepsPerDecimeter (ZAxisLengthInSteps * 1000UL / (ZAxisLengthInMicroM / 100UL))

#define XAxisHomeInMM 143
#define YAxisHomeInMM 147
#define ZAxisHomeInMM 168

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

	axisX.setAxisResolution(XAxisResolutionInStepsPerDecimeter);
	axisY.setAxisResolution(YAxisResolutionInStepsPerDecimeter);
	axisZ.setAxisResolution(ZAxisResolutionInStepsPerDecimeter);
	axisE.setAxisResolution(XAxisResolutionInStepsPerDecimeter);

	axisX.setHomePositionMM(XAxisHomeInMM);
	axisY.setHomePositionMM(YAxisHomeInMM);
	axisZ.setHomePositionMM(ZAxisHomeInMM);
	axisE.setHomePositionMM(XAxisHomeInMM);
}

void RepRapPCB2::update() {
	extenderInput.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();

	extenderOutput.update();
}
