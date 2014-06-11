#ifndef RotaryEncoderAcceleration_h
#define RotaryEncoderAcceleration_h

#include "TicksPerSecond.h"
#include "Button.h"

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
	void initialize(const long minVal = 0, const long maxVal = 1000, const bool looped = false) {
		_isValueLooped = looped;
		_hasValueChanged = true;
		valueChangeEnabled = true;
		value = minVal;
		minValue = minVal;
		maxValue = maxVal;
	}

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
	void setValue_unsafe(long newValue) {
		if (_isValueLooped) {
			long delta = maxValue - minValue + 1;
			if (delta <= 0)
				delta = 1;
			while (newValue > maxValue)
				newValue -= delta;
			while (newValue < minValue)
				newValue += delta;
			value = newValue;
		} else {
			value = constrain(newValue, minValue, maxValue);
		}
		_hasValueChanged = true;
	}

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
	void setMinMax(const long newMinValue, const long newMaxValue) {
		disableInterrupts();
		minValue = newMinValue;
		maxValue = newMaxValue;
		setValue_unsafe(value);
		restoreInterrupts();
	}
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
	/**
	 * Minimum rotary encoder tick per second to start acceleration.
	 * If the speed of ticking is below this value no acceleration
	 * is considered, i.e. ticking is by 1.
	 */
	const int MIN_TPS = 5;

	/**
	 * Maximum rotary encoder tick per second when accelerating.
	 * If the speed of ticking is above this value then acceleration
	 * is considered at full speed.
	 */
	const int MAX_TPS = 30;

	/**
	 * The number of ticks that a rotary encoder should make at full speed
	 * to go from minValue to maxValue. If rotary encoder has 20 ticks for
	 * a 360 degrees rotation then 5 rotations at full speed will be needed
	 * to go from minValue to maxValue.
	 */
	const int TICKS_AT_MAX_SPEED_FOR_FULL_SPAN = 100;

	RotaryEncoderState initialState;
	RotaryEncoderState *state;
public:
	Button pinA;
	Button pinB;
	TicksPerSecond<> tps;

	/**
	 * Initializes the class, sets ports (pinA and pinB) to input mode.
	 */
	void initialize(DigitalInputPin *pinA, DigitalInputPin *pinB) {
		initialState.initialize(0, 1000, false);
		state = &initialState;
		this->pinA.initialize(pinA, 1);
		this->pinB.initialize(pinB, 1);
		tps.initialize();
	}

	/**
	 * Updates the state of the rotary encoder.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update() {
		pinA.update(); // toggle
		pinB.update(); // direction

		if (state->valueChangeEnabled && isTicked()) {
			tps.tick();
			tps.update();
			int speed = constrain(tps.getIntTPS_unsafe(), MIN_TPS, MAX_TPS) - MIN_TPS;
			long delta = max(1, (state->maxValue - state->minValue) / TICKS_AT_MAX_SPEED_FOR_FULL_SPAN);

			// Linear acceleration (very sensitive - not comfortable)
			// long step = 1 + delta * speed / (MAX_TPS - MIN_TPS);

			// Exponential acceleration - square (OK for [maxValue - minValue] = up to 5000)
			// long step = 1 + delta * speed * speed / ((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

			// Exponential acceleration - cubic (most comfortable)
			long step = 1 + delta * speed * speed * speed /
					((MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS) * (MAX_TPS - MIN_TPS));

			state->setValue_unsafe(state->getValue_unsafe() + (isIncrementing() ? step : -step));
		} else {
			tps.update();
		}
	}

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
