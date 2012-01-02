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

const char PROGMEM pgmAxisNameX[] = "X axis";
const char PROGMEM pgmAxisNameY[] = "Y axis";
const char PROGMEM pgmAxisNameZ[] = "Z axis";

const char PROGMEM helpMessage[] =
	"Command format: <command><sub-command>[parameter]<press enter>\n"
	"Where:\n"
	"<command>\n"
	"  x, y, z - use axis x,y or z. See axis commands\n"
	"  a - All (x,y,z) axis\n"
	"  s - Stop all motors\n"
	"  i - Initialize all motors\n"
	"  h - print this Help\n"
	"<axis commands>\n"
	"  s - goto Start position\n"
	"  e - goto End position\n"
	"  d - do Determine available steps\n"
	"  h - goto Home position\n"
	"  p - Print axis status\n"
	"  g - Goto step (parameter long int)\n"
	"  m - Move to position (parameter float - absolute position in millimeters)\n"
	"  y - set delaY between steps at maximum speed (parameter int - delay microseconds)\n"
	"  q - set speed to move the axis from start to end (parameter long - speed in mm/min)\n"
	"-- set the rotary encoder to control a value, only one axis at a time, long click to turn off\n"
	"  G - Goto step\n"
	"  Y - delaY between steps\n"
	"  Q - set speed to move the axis from start to end\n";

static void printHelp() {
	Serial.pgm_println(helpMessage);
}

enum RepRapMode {
	Idle = 0,
	AlterDelayBetweenSteps = 1,
	GotoStep = 2,
	DetermineAvailableSteps = 3,
	MoveForthAndBackAtSpeed = 4,
	InitializeAllMotors = 5,
} repRapMode;
uint8_t modeState;
const char *selectedAxisName;
StepperMotorAxis *selectedAxis;

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
	selectedAxisName = axisName;
	selectedAxis = &axis;
	switch (line++[0]) {
	case 's':
		axis.motorControl.rotate(false);
		break;
	case 'e':
		axis.motorControl.rotate(true);
		break;
	case 'd':
		repRapMode = DetermineAvailableSteps;
		modeState = 0;
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
		long delayBetweenSteps = atol(line);
		axis.setDelayBetweenStepsAtMaxSpeedMicros(delayBetweenSteps);
		axis.motorControl.setDelayBetweenStepsMicros(delayBetweenSteps);
		break;
	}
	case 'Y': {
		rotor.setMinMax(100, 10000);
		rotor.setValue(axis.getDelayBetweenStepsAtMaxSpeedMicros());
		break;
	}
	case 'q': {
		unsigned int speed = atoi(line);
		axis.setSpeed(speed);
		repRapMode = MoveForthAndBackAtSpeed;
		modeState = 0;
		break;
	}
	case 'Q':
		rotor.setMinMax(30, 6000);
		rotor.setValue(axis.getSpeed());
		repRapMode = MoveForthAndBackAtSpeed;
		modeState = 0;
		break;
	case 'G': {
		rotor.setMinMax(0, 10000);
		rotor.setValue(axis.motorControl.getStep());
		break;
	}
	case 'p':
		Serial.pgm_println(PSTR("AXIS state"));
		Serial.pgm_print(PSTR("isMoving:       ")); Serial.println(axis.motorControl.isMoving() ? 'T':'F');
		Serial.pgm_print(PSTR("remaining steps:")); Serial.println(axis.motorControl.remainingSteps);
		Serial.pgm_print(PSTR("cur step:       ")); Serial.println(axis.motorControl.getStep());
		Serial.pgm_print(PSTR("abs position:   ")); Serial.println(axis.getAbsolutePositionMicroM());
		Serial.pgm_print(PSTR("speed (Q):      ")); Serial.println(axis.getSpeed());
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
	repRapMode = Idle;
}

static void doAlterDelayBetweenSteps() {
	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		Serial.pgm_print(selectedAxisName);
		Serial.pgm_print(PSTR(": delay between steps = "));
		Serial.println(val);
		selectedAxis->setDelayBetweenStepsAtMaxSpeedMicros(val);
		selectedAxis->motorControl.setDelayBetweenStepsMicros(val);
	}
}

static void doGotoStep() {
	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		Serial.pgm_print(selectedAxisName);
		Serial.pgm_print(PSTR(": goto step "));
		Serial.println(val);
		selectedAxis->setDelayBetweenStepsAtMaxSpeedMicros(val);
		selectedAxis->motorControl.gotoStep(val);
	}
}

long maxStep;
unsigned long timeStamp;
unsigned long timeToMoveToEndMillis;

static void doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
		// goto begining
		selectedAxis->motorControl.setDelayBetweenStepsMicros(selectedAxis->getDelayBetweenStepsAtMaxSpeedMicros());
		selectedAxis->motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if (!selectedAxis->motorControl.isMoving()) {
			selectedAxis->motorControl.resetStep(0);
			selectedAxis->motorControl.rotate(true);
			timeStamp = millis();
			modeState = 2;
		}
		break;
	case 2:
		if (!selectedAxis->motorControl.isMoving()) {
			maxStep = selectedAxis->motorControl.getStep();
			timeToMoveToEndMillis = millis() - timeStamp;
			timeStamp = millis();
			selectedAxis->motorControl.rotate(false);
			modeState = 3;
		}
		break;
	case 3:
		if (!selectedAxis->motorControl.isMoving()) {
			timeStamp = millis() - timeStamp;
			Serial.print(maxStep);
			Serial.print('\t');
			Serial.print(selectedAxis->motorControl.getStep());
			Serial.print('\t');
			Serial.print(timeToMoveToEndMillis);
			Serial.print('\t');
			Serial.print(timeStamp);
			Serial.println();
			selectedAxis->motorControl.resetStep(0);
			modeState = 0;
		}
		break;
	default:
		stop();
		break;
	}
}

static const long deltaToMove = 50000; // 5 cm
static void doMoveForthAndBackAtSpeed() {
	if (rotor.hasValueChanged()) {
		unsigned int speed = rotor.getValue();
		Serial.pgm_print(selectedAxisName);
		Serial.pgm_print(PSTR(": speed mm/min "));
		Serial.println(speed);
		selectedAxis->motorControl.setDelayBetweenStepsMicros(selectedAxis->speedToDelayBetweenSteps(speed));
	}

	switch (modeState) {
	case 0: {
		long pos = selectedAxis->getAbsolutePositionMicroM() - 2*deltaToMove;
		long timeMicros = ((2 * deltaToMove * selectedAxis->motorControl.getDelayBetweenStepsMicros()) / 1000) * selectedAxis->getAxisResolution() / 100;
		selectedAxis->moveToPositionMicroM(pos, timeMicros);
		modeState = 1;
		break;
	}
	case 1: {
		if (selectedAxis->isIdle()) {
			long pos = selectedAxis->getAbsolutePositionMicroM() + 2 * deltaToMove;
			long timeMicros = ((2 * deltaToMove * selectedAxis->motorControl.getDelayBetweenStepsMicros()) / 1000) * selectedAxis->getAxisResolution() / 100;
			selectedAxis->moveToPositionMicroM(pos, timeMicros);
			modeState = 2;
		}
		break;
	}
	case 2: {
		if (selectedAxis->isIdle()) {
			long pos = selectedAxis->getAbsolutePositionMicroM() + deltaToMove;
			long timeMicros = ((deltaToMove * selectedAxis->motorControl.getDelayBetweenStepsMicros()) / 1000) * selectedAxis->getAxisResolution() / 100;
			selectedAxis->moveToPositionMicroM(pos, timeMicros);
			modeState = 3;
		}
		break;
	}
	case 3: {
		if (selectedAxis->isIdle()) {
			long pos = selectedAxis->getAbsolutePositionMicroM() - deltaToMove;
			long timeMicros = ((deltaToMove * selectedAxis->motorControl.getDelayBetweenStepsMicros()) / 1000) * selectedAxis->getAxisResolution() / 100;
			selectedAxis->moveToPositionMicroM(pos, timeMicros);
			modeState = 2;
		}
		break;
	}
	default:
		break;
	}
}

static void doInitializeAllMotors() {
	switch (modeState) {
	case 0:
		pcb.axisX.motorControl.setDelayBetweenStepsMicros(pcb.axisX.getDelayBetweenStepsAtMaxSpeedMicros());
		pcb.axisY.motorControl.setDelayBetweenStepsMicros(pcb.axisY.getDelayBetweenStepsAtMaxSpeedMicros());
		pcb.axisZ.motorControl.setDelayBetweenStepsMicros(pcb.axisZ.getDelayBetweenStepsAtMaxSpeedMicros());

		pcb.axisX.motorControl.rotate(false);
		pcb.axisY.motorControl.rotate(false);
		pcb.axisZ.motorControl.rotate(false);
		modeState = 1;
		break;
	case 1:
		if (pcb.axisX.isIdle() && pcb.axisY.isIdle() && pcb.axisZ.isIdle()) {
			pcb.axisX.motorControl.resetStep(0);
			pcb.axisY.motorControl.resetStep(0);
			pcb.axisZ.motorControl.resetStep(0);
			repRapMode = Idle;
		}
		break;
	}
}

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

	pcb.initialize();
	repRapMode = Idle;
	repRapMode = Idle;
	selectedAxisName = pgmAxisNameX;
	selectedAxis = &pcb.axisX;

    Serial.pgm_println(PSTR("Initialized"));
    Serial.pgm_println(PSTR("Press the button to stop"));
    printHelp();
}

void RepRapPCB2Test::loop() {
	pcb.update();
	reader.update();
	btn.update();
	led.update();

	if (reader.available()) {
		char *line = reader.readln();
		Serial.println(line);
		switch (line++[0]) {
		case 'i':
			stop();
			repRapMode = InitializeAllMotors;
			modeState= 0;
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
		stop();
		printHelp();
	} else if (btn.isClicked()) {
		stop();
	}

	switch (repRapMode) {
	case AlterDelayBetweenSteps:
		doAlterDelayBetweenSteps();
		break;
	case GotoStep:
		doGotoStep();
		break;
	case DetermineAvailableSteps:
		doDetermineAvailableSteps();
		break;
	case MoveForthAndBackAtSpeed:
		doMoveForthAndBackAtSpeed();
		break;
	case InitializeAllMotors:
		doInitializeAllMotors();
		break;
	case Idle:
	default:
		break;
	}
}
