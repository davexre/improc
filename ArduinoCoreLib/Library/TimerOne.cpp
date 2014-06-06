#include <TimerOne.h>

TimerOne Timer1;

ISR(TIMER1_COMPA_vect) {
	Timer1.timerCallback();
}

