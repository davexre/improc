#ifndef GCodeParser_h
#define GCodeParser_h

#include <stdlib.h>
#include <wiring.h>

////////////// GCODE

#define CommandFlad_G (1<<0)
#define CommandFlad_M (1<<1)
#define CommandFlad_P (1<<2)
#define CommandFlad_X (1<<3)
#define CommandFlad_Y (1<<4)
#define CommandFlad_Z (1<<5)
#define CommandFlad_I (1<<6)
#define CommandFlad_N (1<<7)
#define CommandFlad_CHECKSUM (1<<8)
#define CommandFlad_F (1<<9)
#define CommandFlad_S (1<<10)
#define CommandFlad_Q (1<<11)
#define CommandFlad_R (1<<12)
#define CommandFlad_E (1<<13)
#define CommandFlad_T (1<<14)
#define CommandFlad_J (1<<15)

class GCodeParser {
public:
	enum GCode {
		GCode_NoCommand = -1,
		GCode_MoveRapid = 0,

		// Controlled move; -ve coordinate means zero the axis
		GCode_ControlledMove = 1,

		//go home.  If we send coordinates (regardless of their value) only zero those axes
		GCode_GoHome = 28,
		GCode_Dwell = 4,
//		GCode_UnitsInches = 20,
//		GCode_UnitsMM = 21,
//		GCode_PositioningAbsolute = 90,
//		GCode_PositioningIncremental = 91,
//		GCode_SetPosition = 92,
	};

	enum MCode {
		MCode_NoCommand = -1,
		MCode_CompulsoryStop = 0,
		MCode_OptionalStop = 1,
		MCode_ProgramEnd = 2,
//		MCode_ExtruderForward = 101,
//		MCode_ExtruderReverse = 102,
		MCode_ExtruderSetTemperature = 104,
		MCode_ExtruderGetTemperature = 105,
		MCode_ExtruderFanOn = 106,
		MCode_ExtruderFanOff = 107,
		MCode_ExtruderSetTemperatureAndWait = 109,
		MCode_SetLineNumber = 110,
		MCode_SendDebugInfo = 111,
		MCode_Shutdown = 112,
		MCode_ExtruderSetPWM = 113,
		MCode_GetPosition = 114,
		MCode_GetCapabilities = 115,
		MCode_ExtruderWaitForTemperature = 116,
		MCode_GetZeroPosition = 117,
		MCode_ExtruderOpenValve = 126,
		MCode_ExtruderCloseValve = 127,
		MCode_ExtruderSetTemperature2 = 140,
//		MCode_SetChamberTemperatrue = 141,
//		MCode_SetHoldingPressure = 142,

//		CodeT_SetActiveExtruder0 = 0,
	};

	GCode gCode;
	MCode mCode;

	long X, Y, Z, E;
	long feedRate;		// command F
	float P;			// command P
	float T;			// command T
	float S;			// command S
	float I;			// command I
	float J;			// command J
	float R;			// command R
	float Q;			// command Q
	float N;			// command N

	int commandOccuraceFlag;

	void initialize();
	void initVars();
	bool parse(char *line);
};

#endif
