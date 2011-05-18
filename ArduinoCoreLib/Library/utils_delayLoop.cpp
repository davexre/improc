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
