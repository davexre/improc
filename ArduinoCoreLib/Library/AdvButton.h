#ifndef AdvButton_h
#define AdvButton_h

#include "Button.h"

class AdvButton {
public:
	enum AdvButtonState {
		NONE,
		CLICK,
		LONG_CLICK,
		DOUBLE_CLICK,
		AUTOREPEAT_CLICK
	};

private:
	static const unsigned long ADV_BUTTON_REPEAT_DELAY_MILLIS = 1000;
	static const unsigned long ADV_BUTTON_REPEAT_RATE_MILLIES = 50;
	static const unsigned long ADV_BUTTON_LONG_CLICK_MILLIES = 600;
	static const unsigned long ADV_BUTTON_DBLCLICK_MAX_DELAY = 400;

	Button button;
	unsigned long previousTimeButtonUp;
	unsigned long timeButtonDown;
	unsigned long timeNextAutorepeatToggle;
	bool autoRepeatEnabled;
	bool autoButtonStarted;
	AdvButtonState buttonState;
public:
	void initialize(DigitalInputPin *pin, const bool autoRepeatEnabled, const unsigned int debounceMillis = 10) {
		button.initialize(pin, debounceMillis);
		this->autoRepeatEnabled = autoRepeatEnabled;
		timeNextAutorepeatToggle = previousTimeButtonUp = timeButtonDown = 0;
		autoButtonStarted = false;
		buttonState = NONE;
	}

	void update(void) {
		button.update();
		buttonState = NONE;
		unsigned long now = millis();
		if (button.isToggled()) {
			if (button.isDown()) {
				// Just pressed
				timeButtonDown = now;
				timeNextAutorepeatToggle = now + ADV_BUTTON_REPEAT_DELAY_MILLIS;
				autoButtonStarted = false;
			} else {
				// Just released
				if (!autoButtonStarted) {
					if (now - previousTimeButtonUp <= ADV_BUTTON_DBLCLICK_MAX_DELAY) {
						buttonState = DOUBLE_CLICK;
					} else {
						buttonState = CLICK;
					}
				}
				if (autoButtonStarted && (!autoRepeatEnabled)) {
					// Make sure no double click can be detected AFTER a long click
					previousTimeButtonUp = now - ADV_BUTTON_DBLCLICK_MAX_DELAY - ADV_BUTTON_DBLCLICK_MAX_DELAY;
				} else {
					previousTimeButtonUp = now;
				}
			}
		} else if (button.isDown()) {
			if (autoRepeatEnabled) {
				if (now >= timeNextAutorepeatToggle) {
					timeNextAutorepeatToggle = now + ADV_BUTTON_REPEAT_RATE_MILLIES;
					autoButtonStarted = true;
					buttonState = AUTOREPEAT_CLICK;
				}
			} else {
				if ((!autoButtonStarted) && (now - timeButtonDown >= ADV_BUTTON_LONG_CLICK_MILLIES)) {
					autoButtonStarted = true;
					buttonState = LONG_CLICK;
				}
			}
		}
	}

	inline AdvButtonState getButtonState() {
		return buttonState;
	}

	inline bool isAutoRepeatEnabled() {
		return autoRepeatEnabled;
	}

	inline void setAutoRepeatEnabled(const bool newAutoRepeatEnabled) {
		autoRepeatEnabled = newAutoRepeatEnabled;
	}

	/**
	 * True if double click is detected.
	 * When this method returns true the method isClicked() will ALSO be true.
	 */
	inline bool isDoubleClicked() {
		return (buttonState == DOUBLE_CLICK);
	}

	/**
	 * True if long click is detected.
	 * When this method returns true the method isClicked() will ALSO be true.
	 */
	inline bool isLongClicked() {
		return (buttonState == LONG_CLICK);
	}

	/**
	 * Returns true if buttonState is anything else but AdvButtonState_DOUBLE_CLICK.
	 */
	inline bool isClicked() {
		return (buttonState != NONE);
	}

	/**
	 * Resets the internal timestamps targeted at AutoRepeat, DoubleClick and LongClick
	 */
	void reset() {
		button.reset();
		autoButtonStarted = false;
		timeButtonDown = millis();
		timeNextAutorepeatToggle = timeButtonDown + ADV_BUTTON_REPEAT_DELAY_MILLIS;
		buttonState = NONE;
	}

	inline bool isButtonPressed(void) {
		return button.isPressed();
	}

	inline bool isButtonReleased(void) {
		return button.isReleased();
	}

	inline bool isButtonUp(void) {
		return button.isUp();
	}

	inline bool isButtonDown(void) {
		return button.isDown();
	}

	inline bool isButtonToggled(void) {
		return button.isToggled();
	}
};

#endif
