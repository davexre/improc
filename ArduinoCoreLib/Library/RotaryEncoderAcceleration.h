#ifndef RotaryEncoderAcceleration_h
#define RotaryEncoderAcceleration_h

#include "TicksPerSecond.h"
#include "Button.h"

/**
 * Minimum rotary encoder tick per second to start acceleration.
 * If the speed of ticking is below this value no acceleration
 * is considered, i.e. ticking is by 1.
 */
#define MIN_TPS 5

/**
 * Maximum rotary encoder tick per second when accelerating.
 * If the speed of ticking is above this value then acceleration
 * is considered at full speed.
 */
#define MAX_TPS 30

/**
 * The number of ticks that a rotary encoder should make at full speed
 * to go from minValue to maxValue. If rotary encoder has 20 ticks for
 * a 360 degrees rotation then 5 rotations at full speed will be needed
 * to go from minValue to maxValue.
 */
#define TICKS_AT_MAX_SPEED_FOR_FULL_SPAN 100

class RotaryEncoderAcceleration;

class RotaryEncoderState {
	friend class RotaryEncoderAcceleration;
private:
	long minValue;
	long maxValue;
	volatile long value;
	bool valueChangeEnabled;
	bool _isValueLooped;
	bool _hasValueChanged;
public:
	void initialize(const long minVal = 0, const long maxVal = 1000, const bool looped = false);

	inline void setValueChangeEnabled(const bool newValueChengeEnabled) {
		valueChangeEnabled = newValueChengeEnabled;
	}

	inline bool isValueChangeEnabled() {
		return valueChangeEnabled;
	}

	inline void setValueLooped(const bool newIsValueLooped) {
		_isValueLooped = newIsValueLooped;
	}

	inline bool isValueLooped() {
		return _isValueLooped;
	}

	/**
	 * Returns true if the value has changed since the last call to getValue().
	 */
	inline bool hasValueChanged() {
		return _hasValueChanged;
	}

	/**
	 * Gets the #value# of the encoder. If the update method is called from an
	 * interrupt use the safe method getValue() instead. This method does not
	 * alter the state of the hasValueChanged flag.
	 */
	inline long getValue_unsafe() {
		return value;
	}

	/**
	 * Sets the #value# of the encoder. If the update method is called from an
	 * interrupt use the safe method getValue() instead.
	 */
	void setValue_unsafe(const long newValue);

	inline long peekValue() {
		disableInterrupts();
		long result = value;
		restoreInterrupts();
		return result;
	}

	/**
	 * Gets the #value# of the encoder.
	 */
	inline long getValue() {
		disableInterrupts();
		_hasValueChanged = false;
		long result = value;
		restoreInterrupts();
		return result;
	}

	/**
	 * Sets the #value# of the encoder.
	 */
	inline void setValue(const long newValue) {
		disableInterrupts();
		setValue_unsafe(newValue);
		restoreInterrupts();
	}

	/**
	 * Sets the minValue and maxValue for the rotary encoder and fixes
	 * the #value# if it is out of bounds.

	 */
	void setMinMax(const long newMinValue, const long newMaxValue);
};

/**
 * Quadrature (Rotary encoder) sensitive to rotation speed.
 * When the quadrature is rotated a #value# variable is
 * incremented/decremented. If the quadrature is rotated fast
 * the increment/decrement step is increased.
 *
 * Can be used with or without interrupt!
 *
 * Uses:
 *   the timer0 via the millis() function.
 *   pinA, pinB can be connected to any digital pin.
 *
 *   http://hacks.ayars.org/2009/12/using-quadrature-encoder-rotary-switch.html
 */
class RotaryEncoderAcceleration {
private:
	RotaryEncoderState initialState;
	RotaryEncoderState *state;
public:
	Button pinA;
	Button pinB;
	TicksPerSecond tps;

	/**
	 * Initializes the class, sets ports (pinA and pinB) to input mode.
	 */
	void initialize(DigitalInputPin *pinA, DigitalInputPin *pinB);

	/**
	 * Updates the state of the rotary encoder.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update();

	/**
	 * Has the rotary encoder been ticked at the last update
	 */
	inline bool isTicked() {
		return pinA.isPressed();
	}

	/**
	 * Has the rotary encoder been rotated in incrementing direction at the last update.
	 * If the method returns TRUE the direction is incrementing.
	 */
	inline bool isIncrementing() {
		return pinB.isUp();
	}

	inline RotaryEncoderState* getState() {
		return state;
	}

	inline void setState(RotaryEncoderState* newState) {
		if (newState != NULL) {
			disableInterrupts();
			state = newState;
			restoreInterrupts();
		}
	}

	inline void setValueChangeEnabled(const bool newValueChengeEnabled) {
		state->setValueChangeEnabled(newValueChengeEnabled);
	}

	inline bool isValueChangeEnabled() {
		return state->isValueChangeEnabled();
	}

	inline bool hasValueChanged() {
		return state->hasValueChanged();
	}

	inline long getValue() {
		return state->getValue();
	}

	inline void setValue(const long newValue) {
		state->setValue(newValue);
	}

	inline void setMinMax(const long newMinValue, const long newMaxValue) {
		state->setMinMax(newMinValue, newMaxValue);
	}
};

#endif
