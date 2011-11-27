#include "RepRapPCB.h"

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;

static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

static DigitalOutputArduinoPin diShiftRegisterOutputPinSH;
static DigitalOutputArduinoPin diShiftRegisterOutputPinST;
static DigitalOutputArduinoPin diShiftRegisterOutputPinDS;

static DigitalOutputArduinoPin diShiftRegisterInputPinPE;
static DigitalOutputArduinoPin diShiftRegisterInputPinCP;
static DigitalInputArduinoPin diShiftRegisterInputPinQ7;

void RepRapPCB::initialize() {
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
			SteppingMotor::HalfPower,
			SteppingMotor_MosfetHBridge::DoNotTurnOff,
			extenderOutput.createPinHandler(0),
			extenderOutput.createPinHandler(1),
			extenderOutput.createPinHandler(2),
			extenderOutput.createPinHandler(3));
	motorY.initialize(
			SteppingMotor::HalfPower,
			SteppingMotor_MosfetHBridge::DoNotTurnOff,
			extenderOutput.createPinHandler(4),
			extenderOutput.createPinHandler(5),
			extenderOutput.createPinHandler(6),
			extenderOutput.createPinHandler(7));
	motorZ.initialize(
			SteppingMotor::HalfPower,
			SteppingMotor_MosfetHBridge::DoNotTurnOff,
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));
	motorE.initialize(
			SteppingMotor::HalfPower,
			SteppingMotor_MosfetHBridge::DoNotTurnOff,
			extenderOutput.createPinHandler(12),
			extenderOutput.createPinHandler(13),
			extenderOutput.createPinHandler(14),
			extenderOutput.createPinHandler(15));

	fan = extenderOutput.createPinHandler(16);

	motorX.setMotorCoilTurnOffMicros(40000);
	motorY.setMotorCoilTurnOffMicros(40000);
	motorZ.setMotorCoilTurnOffMicros(40000);
	motorE.setMotorCoilTurnOffMicros(40000);

	axisX.initialize(&motorX, extenderInput.createPinHandler(8), extenderInput.createPinHandler(0));
	axisY.initialize(&motorY, extenderInput.createPinHandler(1), extenderInput.createPinHandler(2));
	axisZ.initialize(&motorZ, extenderInput.createPinHandler(4), extenderInput.createPinHandler(3));
	axisE.initialize(&motorE, extenderInput.createPinHandler(5), extenderInput.createPinHandler(6));

	axisX.motorControl.setDelayBetweenStepsMicros(2000);
	axisY.motorControl.setDelayBetweenStepsMicros(2000);
	axisZ.motorControl.setDelayBetweenStepsMicros(4000);
	axisE.motorControl.setDelayBetweenStepsMicros(2000);

	axisX.setDelayBetweenStepsAtMaxSpeedMicros(2000);
	axisY.setDelayBetweenStepsAtMaxSpeedMicros(2000);
	axisZ.setDelayBetweenStepsAtMaxSpeedMicros(4000);
	axisE.setDelayBetweenStepsAtMaxSpeedMicros(2000);

	axisX.setAxisLengthInMicroM(1000 * 1000L);
	axisY.setAxisLengthInMicroM(1000 * 1000L);
	axisZ.setAxisLengthInMicroM(1000 * 1000L);
	axisE.setAxisLengthInMicroM(1000 * 1000L);

	axisX.setAxisHomePositionMicroM(1000 * 1000L);
	axisY.setAxisHomePositionMicroM(1000 * 1000L);
	axisZ.setAxisHomePositionMicroM(1000 * 1000L);
	axisE.setAxisHomePositionMicroM(1000 * 1000L);

	axisX.setAxisSteps(4576);
	axisY.setAxisSteps(4584);
	axisZ.setAxisSteps(6484);
	axisE.setAxisSteps(1000);

	axisX.setUseStartPositionToInitialize(true);
	axisY.setUseStartPositionToInitialize(true);
	axisZ.setUseStartPositionToInitialize(false);
	axisE.setUseStartPositionToInitialize(true);

	axisX.motorControl.setMaxStepsWithWrongButtonDown(50);
	axisY.motorControl.setMaxStepsWithWrongButtonDown(50);
	axisZ.motorControl.setMaxStepsWithWrongButtonDown(50);
	axisE.motorControl.setMaxStepsWithWrongButtonDown(50);

	axisX.motorControl.setMinDelayBetweenStepsMicros(1000);
	axisY.motorControl.setMinDelayBetweenStepsMicros(1000);
	axisZ.motorControl.setMinDelayBetweenStepsMicros(1000);
	axisE.motorControl.setMinDelayBetweenStepsMicros(1000);
}

void RepRapPCB::update() {
	extenderInput.update();

	motorX.update();
	motorY.update();
	motorZ.update();
	motorE.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();

	extenderOutput.update();
}
