#include "GCodeParser.h"

void GCodeParser::initialize() {
	gCode = CodeG_NoCommand;
	mCode = CodeM_NoCommand;
	X = Y = Z = E = 0;
	P = 0;
}

void GCodeParser::assignFrom(GCodeParser *master) {
	gCode = master->gCode;
	mCode = CodeM_NoCommand;
	X = master->X;
	Y = master->Y;
	Z = master->Z;
	E = master->E;
	feedRate = master->feedRate;
	P = master->P;

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

boolean GCodeParser::parse(char *line) {
	byte checksum = calculateChecksum(line);
	commandOccuraceFlag = 0;
	mCode = CodeM_NoCommand;

	char cmd = *(line++);
	switch (cmd) {
	case 'G':
		gCode = (int) strtol(line, &line, 10);
		commandOccuraceFlag |= CommandFlad_G;
		break;

	case 'M':
		mCode = strtol(line, &line, 10);
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
		X = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_X;
		break;
	case 'Y':
		Y = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_Y;
		break;
	case 'Z':
		Z = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_Z;
		break;
	case 'E':
		E = strtod(line, &line);
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
		feedRate = strtod(line, &line);
		commandOccuraceFlag |= CommandFlad_F;
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