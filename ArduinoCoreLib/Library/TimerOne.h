#ifndef TimerOne_h
#define TimerOne_h

#include <avr/io.h>
#include <avr/interrupt.h>

class TimerOne {
private:
public:
	void initialize();

	inline void attachInterrupt(void (*timerCallback)()) {
		this->timerCallback = timerCallback;
	}

	void detachInterrupt();

	inline void stop() {
		TCCR1B = 0;
	}

	void startWithDelayInCycles(unsigned long delayInCycles);

	inline void startWithDelayInMicros(unsigned long delayInMicros) {
		startWithDelayInCycles(F_CPU);
	}

	void (*timerCallback)();
};

extern TimerOne Timer1;

#endif
