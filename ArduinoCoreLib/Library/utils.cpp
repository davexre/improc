#include "utils.h"

/**
 * unsigned long multiply subroutine - 31 cycles
 */
#define delayLoopExtraCalculations 52
#define delayLoopCPUCyclesPerIteration 10

void delayLoop(const unsigned long millis) {
	unsigned long loop = ((F_CPU / 1000) / delayLoopCPUCyclesPerIteration)
			* millis - delayLoopExtraCalculations;
	while (loop > 0) {
		asm ("NOP;");
		loop--;
	}
}

int myatoi(char **string) {
	char s;
	// skip trailing spaces
	while ((**string) && (**string == ' ')) {
		string[0]++;
	}

	// handle sign
	if (**string == '-') {
		s = 1;
		string[0]++;
	} else {
		s = 0;
	}

	int i = 0;
	char digit;
	while (**string) {
		digit = **string;
		if ((digit < '0') || (digit > '9'))
			break;
		i = (i << 3) + (i << 1) + (digit - '0');
		string[0]++;
	}
	return s ? -i : i;
}

long myatol(char **string) {
	char s;
	// skip trailing spaces
	while ((**string) && (**string == ' ')) {
		string[0]++;
	}

	// handle sign
	if (**string == '-') {
		s = 1;
		string[0]++;
	} else {
		s = 0;
	}

	long i = 0;
	char digit;
	while (**string) {
		digit = **string;
		if ((digit < '0') || (digit > '9'))
			break;
		i = (i << 3) + (i << 1) + (digit - '0');
		string[0]++;
	}
	return s ? -i : i;
}
