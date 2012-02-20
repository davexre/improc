#include "RepRapPCB2.h"

/*
X axis		- lenght 359.8 mm
maxStep	minStep	timeToEndMillis	timeToStartMillis
57539	4	86362	86357
57539	4	86363	86356
57532	1	86352	86351

Y axis		- length 305.9 mm
maxStep	minStep	timeToEndMillis	timeToStartMillis
49036	3	73608	73603
49033	0	73603	73603
49034	1	73605	73603

Z axis		- length 163.8 mm
maxStep	minStep	timeToEndMillis	timeToStartMillis
6337	0	15895	15895
6338	0	15897	15899
6338	1	15898	15895
6337	1	15894	15894
*/

#define XAxisLengthInMicroM 359800UL
#define YAxisLengthInMicroM 305900UL
#define ZAxisLengthInMicroM 163800UL

#define XAxisLengthInSteps 57537
#define YAxisLengthInSteps 49033
#define ZAxisLengthInSteps 6338

//#define XAxisPrecissionInSteps 4
//#define YAxisPrecissionInSteps 10
//#define ZAxisPrecissionInSteps 1

#define XAxisResolutionInStepsPerDecimeter (XAxisLengthInSteps * 1000UL / (XAxisLengthInMicroM / 100UL))
#define YAxisResolutionInStepsPerDecimeter (YAxisLengthInSteps * 1000UL / (YAxisLengthInMicroM / 100UL))
#define ZAxisResolutionInStepsPerDecimeter (ZAxisLengthInSteps * 1000UL / (ZAxisLengthInMicroM / 100UL))

#define XAxisHomeInMM 180
#define YAxisHomeInMM 153
#define ZAxisHomeInMM 168

void RepRapPCB2::initialize() {
	mode = RepRapPCB2::Idle;
	modeState = 0;

	diShiftRegisterOutputPinSH.initialize(shiftRegisterOutputPinSH);
	diShiftRegisterOutputPinST.initialize(shiftRegisterOutputPinST);
	diShiftRegisterOutputPinDS.initialize(shiftRegisterOutputPinDS);
	extenderOutput.initialize(17, DigitalOutputShiftRegister_74HC595::BeforeWriteZeroOnlyModifiedOutputs, // WARNING: This is REALLY important when using H-Bridges!!!
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

	switch (mode) {
	case InitializePosition:
		doInitializePosition();
		break;
	case InitializePositionXY:
		doInitializePositionXY();
		break;
	case Idle:
	default:
		break;
	}
}

bool RepRapPCB2::isIdle() {
	return (mode == RepRapPCB2::Idle) &&
			axisX.isIdle() &&
			axisY.isIdle() &&
			axisZ.isIdle();
//			axisE.isIdle() // TODO:
}

void RepRapPCB2::setMode(RepRapPcbMode mode) {
	this->mode = mode;
	modeState = 0;
}

void RepRapPCB2::doInitializePosition() {
	switch (modeState) {
	case 0:
		axisZ.moveToPositionMicroMFast(axisZ.getAbsolutePositionMicroM() + 5000); // move 5 mm upwards
		modeState = 1;
		break;
	case 1:
		if (axisZ.isIdle()) {
			axisX.initializePosition();
			axisY.initializePosition();
			modeState = 2;
		}
		break;
	case 2:
		if (axisX.isIdle() && axisY.isIdle()) {
			axisZ.initializePosition();
			modeState = 3;
		}
		break;
	case 3:
		if (axisZ.isIdle())
			mode = RepRapPCB2::Idle;
		break;
	}
}

void RepRapPCB2::doInitializePositionXY() {
	switch (modeState) {
	case 0:
		axisX.initializePosition();
		axisY.initializePosition();
		modeState = 1;
		break;
	case 1:
		if (axisX.isIdle() && axisY.isIdle()) {
			mode = RepRapPCB2::Idle;
		}
		break;
	}
}

void RepRapPCB2::moveToXY(long xPositionMicroM, long yPositionMicroM, unsigned int speed) {
	long length = (long) hypot(
			axisX.getAbsolutePositionMicroM() - xPositionMicroM,
			axisY.getAbsolutePositionMicroM() - yPositionMicroM);
	long timeMicros = 1000 * ((60 * length) / speed);
	axisX.moveToPositionMicroM(xPositionMicroM, timeMicros);
	axisY.moveToPositionMicroM(yPositionMicroM, timeMicros);
}

void RepRapPCB2::moveToHomePosition() {
	axisX.moveToHomePosition();
	axisY.moveToHomePosition();
	axisZ.moveToHomePosition();
}

void RepRapPCB2::stop() {
	axisX.stop();
	axisY.stop();
	axisZ.stop();
	axisE.stop();
}

void RepRapPCB2::debugPrint() {
	Serial.pgm_print(PSTR("PCB mode "));
	Serial.print((int)mode);
	Serial.pgm_print(PSTR("/"));
	Serial.println((int)modeState);
	Serial.pgm_print(PSTR("Micros: "));
	Serial.println(micros());
	Serial.println();
	Serial.pgm_print(PSTR("X "));
	axisX.debugPrint();
	Serial.pgm_print(PSTR("Y "));
	axisY.debugPrint();
	Serial.pgm_print(PSTR("Z "));
	axisZ.debugPrint();
}
