#ifndef ROTORACELLERATION3_H_
#define ROTORACELLERATION3_H_

#include <WProgram.h>
#include "TicksPerSecond.h"
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
 *
 *   http://hacks.ayars.org/2009/12/using-quadrature-encoder-rotary-switch.html
 */
class RotorAcelleration3 {
private:
	long minValue;
	long maxValue;
	volatile long position;
public:
	Button pinA;
	Button pinB;
	TicksPerSecond tps;

	void initialize(uint8_t pinNumberA, uint8_t pinNumberB);
	void update();

	inline boolean isTicked() {
		return pinA.isPressed();
	}

	inline boolean isIncrementing() {
		return pinB.isUp();
	}

	inline long getPosition_unsafe() {
		return position;
	}

	inline void setPosition_unsafe(long newPosition) {
		position = constrain(newPosition, minValue, maxValue);
	}

	inline long getPosition() {
		disableInterrupts();
		long result = position;
		restoreInterrupts();
		return result;
	}

	inline void setPosition(long newPosition) {
		disableInterrupts();
		setPosition_unsafe(newPosition);
		restoreInterrupts();
	}

	inline void setMinMax(long newMinValue, long newMaxValue) {
		minValue = newMinValue;
		maxValue = newMaxValue;
		setPosition_unsafe(position);
	}
};

#endif
