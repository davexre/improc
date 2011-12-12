#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "reprap/RepRapPCB2.h"
#include "SerialReader.h"

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

static const unsigned int *states[] = {
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
	"  m - Move to position (parameter float - absolute position in millimeters)\n";

static void printHelp() {
	Serial.pgm_println(helpMessage);
}

static void showError(const char * pgm_msg) {
	Serial.pgm_print(PSTR("Error "));
	Serial.pgm_println(pgm_msg);
	Serial.println();
	printHelp();
}

const char PROGMEM  pgm_Up[] = "Up";
const char PROGMEM  pgm_Down[] = "Down";

static void doAxis(char *line, StepperMotorAxis &axis) {
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
		Serial.pgm_print(PSTR("delay b/n steps:")); Serial.println((int)axis.motorControl.getDelayBetweenStepsMicros());
		Serial.pgm_print(PSTR("axis resolution:")); Serial.println(axis.getAxisResolution());
		Serial.pgm_print(PSTR("home position:  ")); Serial.println(axis.getHomePositionMM());
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

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;

void RepRapPCB2Test::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);
    Serial.pgm_println(PSTR("Initialized"));
    Serial.pgm_println(PSTR("Press the button to stop"));
	pcb.initialize();
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
		case 'a':
			Serial.pgm_println(PSTR("X axis"));
			doAxis(line, pcb.axisX);
			Serial.pgm_println(PSTR("Y axis"));
			doAxis(line, pcb.axisY);
			Serial.pgm_println(PSTR("Z axis"));
			doAxis(line, pcb.axisZ);
			break;
		case 'x':
			doAxis(line, pcb.axisX);
			break;
		case 'y':
			doAxis(line, pcb.axisY);
			break;
		case 'z':
			doAxis(line, pcb.axisZ);
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
		printHelp();
	} else if (btn.isClicked()) {
		stop();
	}
}
