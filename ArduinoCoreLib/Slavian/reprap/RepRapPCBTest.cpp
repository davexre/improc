#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "SteppingMotor.h"
#include "StateLed.h"
#include "reprap/RepRapPCB.h"
#include "SerialReader.h"

DefineClass(RepRapPCBTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static AdvButton btn;
static StateLed led;

static RepRapPCB pcb;
static char readerBuffer[100];
static SerialReader reader;

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static void printHelp() {
	Serial.println("Command format: <command><sub-command>[parameter]<press enter>");
	Serial.println("Where:");
	Serial.println("<command>");
	Serial.println("  x, y, z - use axis x,y or z. See axis commands");
	Serial.println("  a - All (x,y,z) axis");
	Serial.println("  h - print this Help");
	Serial.println("  s - Stop all motors");
	Serial.println("<axis commands>");
	Serial.println("  s - goto Start position");
	Serial.println("  e - goto End position");
	Serial.println("  d - do Determine available steps");
	Serial.println("  h - goto Home position");
	Serial.println("  p - Print axis status");
	Serial.println("  g - Goto step (parameter long int)");
	Serial.println("  m - Move to position (parameter float - absolute position in millimeters)");
	Serial.println("  ");
	Serial.println("  ");
	Serial.println("  ");
	Serial.println("  ");
	Serial.println("  ");
}

static void showError(const char * msg) {
	Serial.print("Error ");
	Serial.println(msg);
	Serial.println();
	printHelp();
}

static void doAxis(char *line, StepperAxis &axis) {
	switch (line++[0]) {
	case 's':
		axis.motorControl.initializeToStartPosition();
		break;
	case 'e':
		axis.motorControl.initializeToEndPosition();
		break;
	case 'd':
		axis.motorControl.determineAvailableSteps();
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
		Serial.println("AXIS state");
		Serial.print("isMoving:     "); Serial.println(axis.isMoving() ? "T":"F");
		Serial.print("isOk:         "); Serial.println(axis.isOk() ? "T":"F");
		Serial.print("axis steps:   "); Serial.println(axis.getAxisSteps());
		Serial.print("axis length:  "); Serial.println(axis.getAxisLengthInMicroM());
		Serial.print("abs position: "); Serial.println(axis.getAbsolutePositionMicroM());
		Serial.print("home position:"); Serial.println(axis.getAxisHomePositionMicroM());
		Serial.println();
		Serial.print("min step:     "); Serial.println(axis.motorControl.getMinStep());
		Serial.print("max step:     "); Serial.println(axis.motorControl.getMaxStep());
		Serial.print("cur step:     "); Serial.println(axis.motorControl.getStep());
		Serial.print("total steps:  "); Serial.println(axis.motorControl.getStepsMadeSoFar());
		Serial.println();
		Serial.print("start button: "); Serial.println(axis.motorControl.startPositionButton->getState() ? "Up" : "Down");
		Serial.print("end button:   "); Serial.println(axis.motorControl.endPositionButton->getState() ? "Up" : "Down");
		Serial.println();
		return;
	default:
		showError("invalid axis command");
		return;
	}
	Serial.println("ok");
}

static void stop() {
	pcb.axisX.stop();
	pcb.axisY.stop();
	pcb.axisZ.stop();
	pcb.axisE.stop();
}

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;

void RepRapPCBTest::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);
    Serial.println("Initialized");
    Serial.println("Press the button to stop");
	pcb.initialize();
    printHelp();
}

void RepRapPCBTest::loop() {
	pcb.update();
	reader.update();
	btn.update();
	led.update();

	if (reader.available()) {
		char *line = reader.readln();
		Serial.println(line);
		switch (line++[0]) {
		case 'a':
			Serial.println("X axis");
			doAxis(line, pcb.axisX);
			Serial.println("Y axis");
			doAxis(line, pcb.axisY);
			Serial.println("Z axis");
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
			showError("invalid command");
			break;
		}
	}

	if (btn.isLongClicked()) {
		printHelp();
	} else if (btn.isClicked()) {
		stop();
	}
}
