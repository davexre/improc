#ifndef SoftwarePWM_h
#define SoftwarePWM_h

#include "DigitalIO.h"

/**
 * Performs a Pulse Width Modulation on a DigitalOutputPin using
 * the program main loop and not a timer. This is non-precise
 * method for PWM but allows for a greater time periods, i.e.
 * controlling a PWM on a frequency of 10 cycles per minute.
 */
class SoftwarePWM {
private:
	static const unsigned long MaxPeriodMillis = 60UL * 1000UL;

	DigitalOutputPin *pin;

	unsigned int frequencyCyclesPerMinute;

	uint8_t value;

	unsigned long toggleTime;
public:
	/**
	 * Initializes the class.
	 *
	 * pin	The pin number the led is attached to.
	 */
	void initialize(DigitalOutputPin *pin, unsigned int frequencyCyclesPerMinute) {
		this->pin = pin;
		pin->setState(0);
		this->frequencyCyclesPerMinute = frequencyCyclesPerMinute;
		this->value = 0;
		this->toggleTime = millis() - MaxPeriodMillis;
	}

	/**
	 * This method should be placed in the main loop.
	 */
	void update(void) {
		bool state = pin->getState();
		unsigned long now = millis();
		if ((value == 0) || (frequencyCyclesPerMinute == 0)) {
			toggleTime = now - MaxPeriodMillis;
			if (state)
				pin->setState(false);
		} else if (value == 255) {
			toggleTime = now - MaxPeriodMillis;
			if (!state)
				pin->setState(true);
		} else {
			unsigned long toggleDelayMillis = ((unsigned long)(state ? value : 255 - value) * 60000UL) /
					((unsigned long) frequencyCyclesPerMinute * 255UL);
			if (now - toggleTime >= toggleDelayMillis) {
				pin->setState(!state);
				toggleTime = now;
			}
		}
	}

	inline void setValue(uint8_t value) {
		this->value = value;
	}

	inline uint8_t getValue() {
		return value;
	}

	inline void setFrequencyCyclesPerMinute(unsigned int frequencyCyclesPerMinute) {
		this->frequencyCyclesPerMinute = frequencyCyclesPerMinute;
	}

	inline unsigned int getFrequencyCyclesPerMinute() {
		return frequencyCyclesPerMinute;
	}

	inline bool isPinOn() {
		return pin->getState();
	}
};

#endif
