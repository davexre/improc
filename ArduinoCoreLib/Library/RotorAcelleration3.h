#ifndef ROTORACELLERATION3_H_
#define ROTORACELLERATION3_H_

#include <WProgram.h>
#include "Button.h"

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
class RotorAcelleration3 {
private:
	long lastTimeToggleA;
public:
	Button pinA;
	Button pinB;
	int steps[3];
	int time0;			// Fastest debounce < timeBetweenPinStateToggle < time0
	int time1;			// Slowest timeBetweenPinStateToggle > time1
	long minValue;
	long maxValue;
	long position;
	void initialize(uint8_t pinNumberA, uint8_t pinNumberB);
	void update();

	inline boolean isTicked() {
		return pinA.isPressed();
	}

	inline boolean isIncrementing() {
		return pinB.isUp();
	}
};

#endif
