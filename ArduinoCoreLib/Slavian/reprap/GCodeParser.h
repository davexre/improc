#ifndef GCodeParser_h
#define GCodeParser_h

#include <stdlib.h>
#include <wiring.h>

////////////// GCODE

#define CodeG_NoCommand -1
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

#define CodeM_NoCommand -1
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
//#define CodeM_SetHoldingPressure 142

//#define CodeT_SetActiveExtruder0 0

//////////

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
	float gCodeUnitsToMM;
	int gCode;

	int mCode;
	float X, Y, Z, E;
	float feedRate;		// command F
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
	boolean parse(char *line);
};

#endif
