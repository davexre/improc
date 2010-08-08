#include "Button.h"

void Button::initialize(int pin) {
	buttonPin = pin;
	pinMode(pin, INPUT);
	digitalWrite(pin, HIGH);
	lastState = buttonState = digitalRead(buttonPin);
}

void Button::update() {
	lastState = buttonState;
	buttonState = digitalRead(buttonPin);
}

/**
 * unsigned long multiply subroutine - 31 cycles
 */
#define delayLoopExtraCalculations 52
#define delayLoopCPUCyclesPerIteration 10

void delayLoop(unsigned long millis) {
	unsigned long loop = ((F_CPU / 1000) / delayLoopCPUCyclesPerIteration)
			* millis - delayLoopExtraCalculations;
	while (loop > 0) {
		asm ("NOP;");
		loop--;
	}
}
