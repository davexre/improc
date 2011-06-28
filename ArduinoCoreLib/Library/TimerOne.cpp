#include <TimerOne.h>
#include <utils.h>

TimerOne Timer1;

ISR(TIMER1_COMPA_vect) {
	Timer1.timerCallback();
}

static void emptyTimerOneRoutine() {
}

void TimerOne::initialize() {
	// Use no clock source, timer diabled.
	timerCallback = &emptyTimerOneRoutine;
	TCCR1A = 0;
	TCCR1B = 0; //_BV(WGM12);
}

void TimerOne::detachInterrupt() {
	stop();
	timerCallback = &emptyTimerOneRoutine;
}

#define TIMERONE_MAXCOMPARE 65536UL

void TimerOne::startWithDelayInCycles(unsigned long delayInCycles) {
	uint8_t clock = 0;
	if (delayInCycles < TIMERONE_MAXCOMPARE)
		clock = _BV(CS10);
	else if ((delayInCycles >>= 3) < TIMERONE_MAXCOMPARE)
		clock = _BV(CS11);
	else if ((delayInCycles >>= 3) < TIMERONE_MAXCOMPARE)
		clock = _BV(CS11) | _BV(CS10);
	else if ((delayInCycles >>= 2) < TIMERONE_MAXCOMPARE)
		clock = _BV(CS12);
	else if ((delayInCycles >>= 2) < TIMERONE_MAXCOMPARE)
		clock = _BV(CS12) | _BV(CS10);
	else {
		// request was out of bounds, set the maximum delay
		delayInCycles = TIMERONE_MAXCOMPARE - 1;
		clock = _BV(CS12) | _BV(CS10);
	}
	// Use the "Clear Timer on Compare Match (CTC) Mode", p.125 in ATmega328.pdf
	disableInterrupts();
	TCCR1B = _BV(WGM12) | clock;
	OCR1A = (unsigned int) delayInCycles;
	TIMSK1 = _BV(OCIE1A);
	TCNT1 = 0;
	restoreInterrupts();
}
