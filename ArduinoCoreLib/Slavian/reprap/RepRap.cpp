#include "Arduino.h"
#include "StateLed.h"
#include "AdvButton.h"
#include "SerialReader.h"

DefineClass(RepRap);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static AdvButton btn;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3,
		BLINK_ON, BLINK_OFF
};

static SerialReader reader;
static char buf[200];

void RepRap::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);
	reader.initialize(115200, size(buf), buf);
}

////////////// GCODE

int lastGCode = -1;

#define CodeG_MoveRapid 0

// Controlled move; -ve coordinate means zero the axis
#define CodeG_ControlledMove 1

//go home.  If we send coordinates (regardless of their value) only zero those axes
#define CodeG_GoHome 28
#define CodeG_Dwell 4
#define CodeG_UnitsInches 20
#define CodeG_UnitsMM 21
#define CodeG_PositioningAbsolute 90
#define CodeG_PositioningIncremental 91
#define CodeG_SetPosition 92

#define CodeM_CompulsoryStop 0
#define CodeM_OptionalStop 1
#define CodeM_ProgramEnd 2
//#define CodeM_ExtruderForward 101
//#define CodeM_ExtruderReverse 102
#define CodeM_ExtruderSetTemperature 104
#define CodeM_ExtruderGetTemperature 105
#define CodeM_ExtruderFanOn 106
#define CodeM_ExtruderFanOff 107
#define CodeM_ExtruderSetTemperatureAndWait 109
#define CodeM_SetLineNumber 110
#define CodeM_SendDebugInfo 111
#define CodeM_Shutdown 112
#define CodeM_ExtruderSetPWM 113
#define CodeM_GetPosition 114
#define CodeM_GetCapabilities 115
#define CodeM_ExtruderWaitForTemperature 116
#define CodeM_GetZeroPosition 117
#define CodeM_ExtruderOpenValve 126
#define CodeM_ExtruderCloseValve 127
#define CodeM_ExtruderSetTemperature2 140
//#define CodeM_SetChamberTemperatrue 141
#define CodeM_SetHoldingPressure 142

//#define CodeT_SetActiveExtruder0 0

byte calculateChecksum(char *line) {
	byte result = 0;
	while (true) {
		char c = *(line++);
		if ((c == '*') || (c == ';') || (c == '/') || (c == 0))
			return result;
		result ^= c;
	}
}

void gCodeProcessLine(char *line) {
	char cmd = *(line++);
	switch (cmd) {
	case 'G':
		lastGCode = (int) strtol(line, &line, 10);
		break;

	case 'M':
		strtol(line, &line, 10);
		break;

	case 'T':
		strtol(line, &line, 10);
		break;

	case 'S':
		strtod(line, &line);
		break;

	case 'P':
		strtod(line, &line);
		break;

	case 'X':
		strtod(line, &line);
		break;

	case 'Y':
		strtod(line, &line);
		break;

	case 'Z':
		strtod(line, &line);
		break;

	case 'I':
		strtod(line, &line);
		break;

	case 'J':
		strtod(line, &line);
		break;

	case 'F':
		strtod(line, &line);
		break;

	case 'R':
		strtod(line, &line);
		break;

	case 'Q':
		strtod(line, &line);
		break;

	case 'E':
		strtod(line, &line);
		break;

	case 'N':
		strtol(line, &line, 10);
		break;

	case '*':
		strtol(line, &line, 10);
		break;

	case ';': // This is comment ignore till the end of line
	case '/':
	case 0: // Empty string
	default:
		break;
	}
}

void RepRap::loop() {
	btn.update();
	led.update();
	reader.update();

	if (reader.available()) {
		char *c = reader.readln();
		Serial.println(c);
	}

	if (btn.isClicked()) {
	}
}
