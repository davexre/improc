#ifndef ROTORACELLERATION_H_
#define ROTORACELLERATION_H_

#include <WProgram.h>

/**
 * Quadrature (Rotor knob) sensitive to rotation speed.
 * When the quadrature is rotated a #position# variable is
 * incremented/decremented. If the quadrature is rotated fast
 * the increment/decrement step is increased.
 *
 * Uses:
 *   the timer0 via the millis() function.
 *   pinA of the quadrature must be connected
 *   		to pin0 or pin1 (hardware interrupt)
 *   pinB can be connected to any other digital pin.
 *
 *
 */
class RotorAcelleration {
private:
	static void UpdateRotation();
	int lastDirection;
	long lastTime;
public:
	int pinA;
	int pinB;
	int steps[3];
	int timeDebounce;	// Below this time will ignore interrupts
	int time0;			// Fastest timeDebounce < timeBetweenInterrupts < time0
	int time1;			// Slowest timeBetweenInterrupts > time1
	long minValue;
	long maxValue;
	volatile long position;
	void initialize(int pinA, int pinB);
	void update();

};

extern RotorAcelleration rotor;

#endif
