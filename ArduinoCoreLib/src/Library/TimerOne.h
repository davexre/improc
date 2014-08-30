#ifndef TimerOne_h
#define TimerOne_h

#include <avr/io.h>
#include <avr/interrupt.h>
#include <utils.h>



class TimerOne {
private:
	static void emptyTimerOneRoutine() {
	}

	static const unsigned long TIMERONE_MAXCOMPARE = 65536;
public:
	void initialize() {
		// Use no clock source, timer diabled.
		timerCallback = &TimerOne::emptyTimerOneRoutine;
		TCCR1A = 0;
		TCCR1B = 0; //_BV(WGM12);
	}

	void attachInterrupt(void (*timerCallbackRoutine)()) {
		timerCallback = timerCallbackRoutine;
	}

	void detachInterrupt() {
		stop();
		timerCallback = &TimerOne::emptyTimerOneRoutine;
	}

	static inline void stop() {
		TCCR1B = 0;
	}

	static bool isPlaying(void) {
		return TIMSK1 & _BV(OCIE1A);
	}

	static void startWithDelayInCycles(unsigned long delayInCycles) {
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

	static inline void startWithDelayInMicros(const unsigned long delayInMicros) {
		startWithDelayInCycles(delayInMicros * (F_CPU / 1000000UL));
	}

	static inline void startWithDelayInMillis(const unsigned long delayInMillis) {
		startWithDelayInCycles(delayInMillis * (F_CPU / 1000UL));
	}

	static inline void startWithFrequency(const unsigned int frequencyInHertz) {
		startWithDelayInCycles(F_CPU / frequencyInHertz - 1);
	}

	void (*timerCallback)();
};

extern TimerOne Timer1;

#endif
