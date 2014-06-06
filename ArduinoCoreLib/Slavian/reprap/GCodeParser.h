#ifndef GCodeParser_h
#define GCodeParser_h

#include <stdlib.h>

////////////// GCODE
enum GCodeCommandFlag {
	CommandFlad_G = (1L<<0),
	CommandFlad_M = (1L<<1),
	CommandFlad_P = (1L<<2),
	CommandFlad_X = (1L<<3),
	CommandFlad_Y = (1L<<4),
	CommandFlad_Z = (1L<<5),
	CommandFlad_I = (1L<<6),
	CommandFlad_N = (1L<<7),
	CommandFlad_CHECKSUM = (1L<<8),
	CommandFlad_F = (1L<<9),
	CommandFlad_S = (1L<<10),
	CommandFlad_Q = (1L<<11),
	CommandFlad_R = (1L<<12),
	CommandFlad_E = (1L<<13),
	CommandFlad_T = (1L<<14),
	CommandFlad_J = (1L<<15),
	CommandFlad_K = (1L<<16)
};

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
	long speed;			// ???
	float P;			// command P
	float T;			// command T
	float S;			// command S
	float I;			// command I
	float J;			// command J
	float R;			// command R
	float Q;			// command Q
	float N;			// command N

	long commandOccuraceFlag;

	void initialize() {
		gCode = GCodeParser::GCode_NoCommand;
		initVars();
	}

	void initVars() {
		commandOccuraceFlag = 0;
		mCode = GCodeParser::MCode_NoCommand;
		speed = feedRate = X = Y = Z = E = 0;
		P = T = S = I = J = R = Q = N = 0;
	}

	static byte calculateChecksum(const char *line) {
		byte result = 0;
		while (true) {
			char c = *(line++);
			if ((c == '*') || (c == ';') || (c == '/') || (c == 0))
				return result;
			result ^= c;
		}
	}

	bool parse(char *line) {
		initVars();
		byte checksum = calculateChecksum(line);
		char cmd = *(line++);
		switch (cmd) {
		case 'G':
			gCode = (GCodeParser::GCode) strtol(line, &line, 10);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_G;
			break;

		case 'M':
			mCode = (GCodeParser::MCode) strtol(line, &line, 10);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_M;
			break;

		case 'T':
			T = strtol(line, &line, 10);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_T;
			break;

		case 'S':
			S = strtod(line, &line);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_S;
			break;

		case 'P':
			P = strtod(line, &line);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_P;
			break;

		case 'X':
			X = (long) (strtod(line, &line) * 1000.0f);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_X;
			break;
		case 'Y':
			Y = (long) (strtod(line, &line) * 1000.0f);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_Y;
			break;
		case 'Z':
			Z = (long) (strtod(line, &line) * 1000.0f);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_Z;
			break;
		case 'E':
			E = (long) (strtod(line, &line) * 1000.0f);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_E;
			break;

		case 'I':
			I = strtod(line, &line);
			commandOccuraceFlag |= GCodeCommandFlag::CommandFlad_I;
			break;

		case 'J':
			J = strtod(line, &line);
			commandOccuraceFlag |= CommandFlad_J;
			break;

		case 'F':
			feedRate = (long) strtod(line, &line); // TODO: may be a multiplier constant is needed
			commandOccuraceFlag |= CommandFlad_F;
			break;

		case 'K':
			speed = (long) strtod(line, &line); // TODO: may be a multiplier constant is needed
			commandOccuraceFlag |= CommandFlad_K;
			break;

		case 'R':
			R = strtod(line, &line);
			commandOccuraceFlag |= CommandFlad_R;
			break;

		case 'Q':
			Q = strtod(line, &line);
			commandOccuraceFlag |= CommandFlad_Q;
			break;

		case 'N':
			N = strtol(line, &line, 10);
			commandOccuraceFlag |= CommandFlad_N;
			break;

		case '*':
			if (strtol(line, &line, 10) != checksum)
				return false;
	//		commandOccuraceFlag |= CommandFlad_CHECKSUM;
			break;

		case ';': // This is comment ignore till the end of line
		case '/':
		case 0: // Empty string
		default:
			break;
		}

		return true;
	}
};

#endif
