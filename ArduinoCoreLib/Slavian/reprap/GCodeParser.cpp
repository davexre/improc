#include "Arduino.h"
#include "GCodeParser.h"

void GCodeParser::initialize() {
	gCode = GCodeParser::GCode_NoCommand;
	initVars();
}

void GCodeParser::initVars() {
	commandOccuraceFlag = 0;
	mCode = GCodeParser::MCode_NoCommand;
	speed = feedRate = X = Y = Z = E = 0;
	P = T = S = I = J = R = Q = N = 0;
}

static byte calculateChecksum(char *line) {
	byte result = 0;
	while (true) {
		char c = *(line++);
		if ((c == '*') || (c == ';') || (c == '/') || (c == 0))
			return result;
		result ^= c;
	}
}

bool GCodeParser::parse(char *line) {
	initVars();
	byte checksum = calculateChecksum(line);
	char cmd = *(line++);
	switch (cmd) {
	case 'G':
		gCode = (GCodeParser::GCode) strtol(line, &line, 10);
		commandOccuraceFlag |= CommandFlad_G;
		break;

	case 'M':
		mCode = (GCodeParser::MCode) strtol(line, &line, 10);
		commandOccuraceFlag |= CommandFlad_M;
		break;

	case 'T':
		T = strtol(line, &line, 10);
		commandOccuraceFlag |= CommandFlad_T;
		break;

	case 'S':
		S = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_S;
		break;

	case 'P':
		P = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_P;
		break;

	case 'X':
		X = (long) (strtod(line, &line) * 1000.0f);
		commandOccuraceFlag |= CommandFlad_X;
		break;
	case 'Y':
		Y = (long) (strtod(line, &line) * 1000.0f);
		commandOccuraceFlag |= CommandFlad_Y;
		break;
	case 'Z':
		Z = (long) (strtod(line, &line) * 1000.0f);
		commandOccuraceFlag |= CommandFlad_Z;
		break;
	case 'E':
		E = (long) (strtod(line, &line) * 1000.0f);
		commandOccuraceFlag |= CommandFlad_E;
		break;

	case 'I':
		I = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_I;
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
