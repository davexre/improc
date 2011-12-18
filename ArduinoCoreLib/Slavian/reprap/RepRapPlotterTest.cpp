#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "reprap/RepRapPCB2.h"
#include "SerialReader.h"
#include "RotaryEncoderAcceleration.h"

DefineClass(RepRapPlotterTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static AdvButton btn;
static StateLed led;

static RepRapPCB2 pcb;
static char readerBuffer[100];
static SerialReader reader;
static RotaryEncoderAcceleration rotor;

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK_MEDIUM,
//		BLINK1, BLINK2, BLINK3
};

enum RepRapMode {
	Idle = 0,
	InitXY = 1,
	InitZ = 2,
	InitXYZ = 3,
	WaitForMotors = 4,
} repRapMode;
uint8_t modeState;
StepperMotorAxis *selectedAxis;

const char PROGMEM pgm_ok[] = "ok";

static void UpdateRotor() {
	rotor.update();
}

void RepRapPlotterTest::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	attachInterrupt(0, UpdateRotor, CHANGE);

	pcb.initialize();
	repRapMode = InitXYZ;
	selectedAxis = &pcb.axisX;

    Serial.pgm_println(PSTR("Initialized"));
    Serial.pgm_println(pgm_ok);
}

static void update() {
	pcb.update();
	reader.update();
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
	} else if (btn.isClicked()) {
	}
}

inline long pow2(long x) {
	return x*x;
}

long speed = 1000;
static void doReader() {
	if (reader.available()) {
		modeState = 0;
		char *line = reader.readln();
		switch (line++[0]) {
		case 'I':
			repRapMode = InitXY;
			break;
		case 'Z':
			repRapMode = InitZ;
			break;
		case 'S':
			speed = strtol(line, &line, 10);
			break;
		case 'U':
			pcb.axisZ.moveToPositionMicroMFast(5000);
			repRapMode = WaitForMotors;
			break;
		case 'D':
			pcb.axisZ.moveToPositionMicroMFast(0);
			repRapMode = WaitForMotors;
			break;
		case 'M': {
			long x = strtol(line, &line, 10);
			line++;
			long y = strtol(line, &line, 10);
			long length = (long) sqrt(pow2(pcb.axisX.getAbsolutePositionMicroM() - x) +
					pow2(pcb.axisY.getAbsolutePositionMicroM() - y));
			long timeMicros = ((60000 * length) / speed) * 1000;
			pcb.axisX.moveToPositionMicroM(x, timeMicros);
			pcb.axisY.moveToPositionMicroM(y, timeMicros);
			repRapMode = WaitForMotors;
			break;
		}
		case 'H':
			pcb.axisX.moveToHomePosition();
			pcb.axisY.moveToHomePosition();
			pcb.axisZ.moveToHomePosition();
			repRapMode = WaitForMotors;
			break;
		default:
			break;
		}
	}
}

static void doInitXY() {
	switch (modeState) {
	case 0:
		pcb.axisX.motorControl.rotate(false);
		pcb.axisY.motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if ((!pcb.axisX.motorControl.isMoving()) &&
			(!pcb.axisY.motorControl.isMoving())) {
			pcb.axisX.motorControl.resetStep(0);
			pcb.axisY.motorControl.resetStep(0);
			Serial.pgm_println(pgm_ok);
			repRapMode = Idle;
		}
		break;
	}
}

static void doInitZ() {
	switch (modeState) {
	case 0:
		pcb.axisZ.motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if ((!pcb.axisZ.motorControl.isMoving())) {
			pcb.axisZ.motorControl.resetStep(0);
			Serial.pgm_println(pgm_ok);
			repRapMode = Idle;
		}
		break;
	}
}

static void doInitXYZ() {
	switch (modeState) {
	case 0:
		pcb.axisX.motorControl.rotate(false);
		pcb.axisY.motorControl.rotate(false);
		pcb.axisZ.motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if ((!pcb.axisX.motorControl.isMoving()) &&
			(!pcb.axisY.motorControl.isMoving()) &&
			(!pcb.axisZ.motorControl.isMoving())) {
			pcb.axisX.motorControl.resetStep(0);
			pcb.axisY.motorControl.resetStep(0);
			pcb.axisZ.motorControl.resetStep(0);
			Serial.pgm_println(pgm_ok);
			repRapMode = Idle;
		}
		break;
	}
}

void RepRapPlotterTest::loop() {
	update();

	switch (repRapMode) {
	case InitXY:
		doInitXY();
		break;
	case InitZ:
		doInitZ();
		break;
	case InitXYZ:
		doInitXYZ();
		break;
	case WaitForMotors:
		if (pcb.axisX.isIdle() && pcb.axisY.isIdle() && pcb.axisZ.isIdle()) {
			Serial.pgm_println(pgm_ok);
			repRapMode = Idle;
		}
		break;

	case Idle:
	default:
		doReader();
		break;
	}
}
