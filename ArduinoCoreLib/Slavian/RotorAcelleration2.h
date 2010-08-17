#ifndef ROTORACELLERATION_H_
#define ROTORACELLERATION_H_

#include <WProgram.h>

/**
 * Quadrature (Rotor knob) sensitive to rotation speed.
 * When the quadrature is rotated a #position# variable is
 * incremented/decremented. If the quadrature is rotated fast
 * the increment/decrement step is increased.
 *
 * No interrupt is used!
 *
 * Uses:
 *   the timer0 via the millis() function.
 *   pinA, pinB can be connected to any digital pin.
 */
class RotorAcelleration2 {
private:
	boolean pinAState;
	boolean pinBState;
	long lastTimeToggleA;
	long lastTimeToggleB;
public:
	int pinA;
	int pinB;
	int steps[3];
	int debounce;		// time in millis, default 7
	int time0;			// Fastest debounce < timeBetweenPinStateToggle < time0
	int time1;			// Slowest timeBetweenPinStateToggle > time1
	long minValue;
	long maxValue;
	long position;
	void initialize(int pinA, int pinB);
	void update();
};

#endif
