#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "reprap/RepRapPCB2.h"
#include "SerialReader.h"
#include "RotaryEncoderAcceleration.h"

DefineClass(RepRapPCB2Test);

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

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK_MEDIUM,
//		BLINK1, BLINK2, BLINK3
};

const char PROGMEM helpMessage[] =
	"Command format: <command><sub-command>[parameter]<press enter>\n"
	"Where:\n"
	"<command>\n"
	"  x, y, z - use axis x,y or z. See axis commands\n"
	"  a - All (x,y,z) axis\n"
	"  h - print this Help\n"
	"  s - Stop all motors\n"
	"<axis commands>\n"
	"  s - goto Start position\n"
	"  e - goto End position\n"
	"  d - do Determine available steps\n"
	"  h - goto Home position\n"
	"  p - Print axis status\n"
	"  g - Goto step (parameter long int)\n"
	"  m - Move to position (parameter float - absolute position in millimeters)\n"
	"  y - set delaY between steps at maximum speed (parameter int - delay microseconds)\n"
	"-- set the rotary encoder to control a value, only one axis at a time, long click to turn off\n"
	"  Y - delaY between steps\n"
	"  G - Goto step\n";

static void printHelp() {
	Serial.pgm_println(helpMessage);
}

enum RotorMode {
	Idle = 0,
	AlterDelayBetweenStepsX = 1,
	AlterDelayBetweenStepsY = 2,
	AlterDelayBetweenStepsZ = 3,

	GotoStepX = 5,
	GotoStepY = 6,
	GotoStepZ = 7,
} rotorMode;

static void showError(const char * pgm_msg) {
	Serial.pgm_print(PSTR("Error "));
	Serial.pgm_println(pgm_msg);
	Serial.println();
	printHelp();
}

const char PROGMEM pgm_Up[] = "Up";
const char PROGMEM pgm_Down[] = "Down";

static void UpdateRotor() {
	rotor.update();
}

static void doAxis(const char *axisName, char *line, StepperMotorAxis &axis) {
	Serial.pgm_println(axisName);
	switch (line++[0]) {
	case 's':
		axis.motorControl.rotate(false);
		break;
	case 'e':
		axis.motorControl.rotate(true);
		break;
	case 'd':
		axis.determineAvailableSteps();
		break;
	case 'h':
		axis.moveToHomePosition();
		break;
	case 'g': {
		long step = atol(line);
		axis.motorControl.gotoStep(step);
		break;
	}
	case 'm': {
		long pos = atof(line) * 1000.0;
		axis.moveToPositionMicroMFast(pos);
		break;
	}
	case 'y': {
		long maxSpeed = atol(line);
		axis.setDelayBetweenStepsAtMaxSpeedMicros(maxSpeed);
		axis.motorControl.setDelayBetweenStepsMicros(maxSpeed);
		break;
	}
	case 'Y': {
		rotor.setMinMax(100, 10000);
		rotor.setValue(axis.getDelayBetweenStepsAtMaxSpeedMicros());
		if (&axis == &pcb.axisX)
			rotorMode = AlterDelayBetweenStepsX;
		else if (&axis == &pcb.axisY)
			rotorMode = AlterDelayBetweenStepsY;
		else if (&axis == &pcb.axisZ)
			rotorMode = AlterDelayBetweenStepsZ;
		break;
	}
	case 'G': {
		rotor.setMinMax(0, 10000);
		rotor.setValue(axis.motorControl.getStep());
		if (&axis == &pcb.axisX)
			rotorMode = GotoStepX;
		else if (&axis == &pcb.axisY)
			rotorMode = GotoStepY;
		else if (&axis == &pcb.axisZ)
			rotorMode = GotoStepZ;
		break;
	}
	case 'p':
		Serial.pgm_println(PSTR("AXIS state"));
		Serial.pgm_print(PSTR("isMoving:       ")); Serial.println(axis.motorControl.isMoving() ? 'T':'F');
		Serial.pgm_print(PSTR("remaining steps:")); Serial.println(axis.motorControl.remainingSteps);
		Serial.pgm_print(PSTR("cur step:       ")); Serial.println(axis.motorControl.getStep());
		Serial.pgm_print(PSTR("abs position:   ")); Serial.println(axis.getAbsolutePositionMicroM());
		Serial.println();
		Serial.pgm_print(PSTR("start button:   ")); Serial.pgm_println(axis.motorControl.startButton->getState() ? pgm_Up : pgm_Down);
		Serial.pgm_print(PSTR("end button:     ")); Serial.pgm_println(axis.motorControl.endButton->getState() ? pgm_Up : pgm_Down);
		Serial.println();
		Serial.pgm_print(PSTR("movement mode:  ")); Serial.println((int)axis.motorControl.movementMode);
		Serial.pgm_print(PSTR("axis resolution:")); Serial.println(axis.getAxisResolution());
		Serial.pgm_print(PSTR("home position:  ")); Serial.println(axis.getHomePositionMM());
		Serial.pgm_print(PSTR("delay b/n steps:")); Serial.println(axis.motorControl.getDelayBetweenStepsMicros());
		Serial.pgm_print(PSTR("delay b/n steps@max speed:")); Serial.println(axis.getDelayBetweenStepsAtMaxSpeedMicros());
		Serial.println();
		return;
	default:
		showError(PSTR("invalid axis command"));
		return;
	}
	Serial.pgm_println(PSTR("ok"));
}

static void stop() {
	pcb.axisX.stop();
	pcb.axisY.stop();
	pcb.axisZ.stop();
	pcb.axisE.stop();
}

static void doAlterDelayBetweenSteps(const char *axisName, StepperMotorAxis &axis) {
	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		Serial.pgm_print(axisName);
		Serial.pgm_print(PSTR(": delay between steps = "));
		Serial.println(val);
		axis.setDelayBetweenStepsAtMaxSpeedMicros(val);
		axis.motorControl.setDelayBetweenStepsMicros(val);
	}
}

static void doGotoStep(const char *axisName, StepperMotorAxis &axis) {
	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		Serial.pgm_print(axisName);
		Serial.pgm_print(PSTR(": goto step "));
		Serial.println(val);
		axis.setDelayBetweenStepsAtMaxSpeedMicros(val);
		axis.motorControl.gotoStep(val);
	}
}

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

void RepRapPCB2Test::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	attachInterrupt(0, UpdateRotor, CHANGE);
	rotorMode = Idle;

	pcb.initialize();
    Serial.pgm_println(PSTR("Initialized"));
    Serial.pgm_println(PSTR("Press the button to stop"));
    printHelp();
}

const char PROGMEM pgmAxisNameX[] = "X axis";
const char PROGMEM pgmAxisNameY[] = "Y axis";
const char PROGMEM pgmAxisNameZ[] = "Z axis";

void RepRapPCB2Test::loop() {
	pcb.update();
	reader.update();
	btn.update();
	led.update();

	if (reader.available()) {
		char *line = reader.readln();
		Serial.println(line);
		switch (line++[0]) {
		case 'a':
			doAxis(pgmAxisNameX, line, pcb.axisX);
			doAxis(pgmAxisNameY, line, pcb.axisY);
			doAxis(pgmAxisNameZ, line, pcb.axisZ);
			break;
		case 'x':
			doAxis(pgmAxisNameX, line, pcb.axisX);
			break;
		case 'y':
			doAxis(pgmAxisNameY, line, pcb.axisY);
			break;
		case 'z':
			doAxis(pgmAxisNameZ, line, pcb.axisZ);
			break;
		case 'h':
			printHelp();
			break;
		case 's':
			stop();
			break;
		default:
			showError(PSTR("invalid command"));
			break;
		}
	}

	if (btn.isLongClicked()) {
		rotorMode = Idle;
		printHelp();
	} else if (btn.isClicked()) {
		stop();
	}

	switch (rotorMode) {
	case AlterDelayBetweenStepsX:
		doAlterDelayBetweenSteps(pgmAxisNameX, pcb.axisX);
		break;
	case AlterDelayBetweenStepsY:
		doAlterDelayBetweenSteps(pgmAxisNameY, pcb.axisY);
		break;
	case AlterDelayBetweenStepsZ:
		doAlterDelayBetweenSteps(pgmAxisNameZ, pcb.axisZ);
		break;
	case GotoStepX:
		doGotoStep(pgmAxisNameX, pcb.axisX);
		break;
	case GotoStepY:
		doGotoStep(pgmAxisNameY, pcb.axisY);
		break;
	case GotoStepZ:
		doGotoStep(pgmAxisNameZ, pcb.axisZ);
		break;
	case Idle:
	default:
		break;
	}
}
