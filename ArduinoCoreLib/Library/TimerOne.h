#ifndef TimerOne_h
#define TimerOne_h

#include <avr/io.h>
#include <avr/interrupt.h>

class TimerOne {
public:
	void initialize();

	void attachInterrupt(void (*timerCallbackRoutine)()) {
		timerCallback = timerCallbackRoutine;
	}

	void detachInterrupt();

	static inline void stop() {
		TCCR1B = 0;
	}

	static bool isPlaying(void) {
		return TIMSK1 & _BV(OCIE1A);
	}

	static void startWithDelayInCycles(unsigned long delayInCycles);

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
