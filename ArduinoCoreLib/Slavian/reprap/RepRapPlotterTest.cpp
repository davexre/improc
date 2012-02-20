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
	WaitForMotors = 1,
} repRapMode;
uint8_t modeState;

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
	repRapMode = WaitForMotors;

    Serial.pgm_println(PSTR("Initialized"));
}

long speed = 200;		// in mm/min
static void doReader() {
	if (reader.available()) {
		char *line = reader.readln();
		switch (line++[0]) {
		case 'I':
			pcb.initializePositionXY();
			break;
		case 'Z':
			pcb.initializePosition();
			break;
		case 'S':
			speed = strtol(line, &line, 10);
			break;
		case 'U':
			pcb.axisZ.moveToPositionMicroMFast(5000);
			break;
		case 'D':
			pcb.axisZ.moveToPositionMicroMFast(0);
			break;
		case 'M': {
			long x = strtol(line, &line, 10);
			line++;
			long y = strtol(line, &line, 10);
			pcb.moveToXY(x, y, speed);
			break;
		}
		case 'H':
			pcb.moveToHomePosition();
			break;
		default:
			Serial.pgm_print(PSTR("ERR: "));
			Serial.println(line);
			return;
		}
		repRapMode = WaitForMotors;
	}
}

void RepRapPlotterTest::loop() {
	pcb.update();
	reader.update();
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		pcb.debugPrint();
	} else if (btn.isClicked()) {
		pcb.stop();
	}

	switch (repRapMode) {
	case WaitForMotors:
		if (pcb.isIdle()) {
			Serial.pgm_println(PSTR("ok"));
			repRapMode = Idle;
		}
		break;

	case Idle:
	default:
		doReader();
		break;
	}
}
