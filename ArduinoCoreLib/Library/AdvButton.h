#ifndef AdvButton_h
#define AdvButton_h

#include "Button.h"

#define ADV_BUTTON_REPEAT_DELAY_MILLIS 1000
#define ADV_BUTTON_REPEAT_RATE_MILLIES 50

#define ADV_BUTTON_LONG_CLICK_MILLIES 600

#define ADV_BUTTON_DBLCLICK_MAX_DELAY 400

enum AdvButtonState {
	AdvButtonState_NONE,
	AdvButtonState_CLICK,
	AdvButtonState_LONG_CLICK,
	AdvButtonState_DOUBLE_CLICK,
	AdvButtonState_AUTOREPEAT_CLICK
};

class AdvButton {
	long previousTimeButtonUp;
	long timeButtonDown;
	long timeNextAutorepeatToggle;
	boolean autoRepeatEnabled;
	boolean autoButtonStarted;
	byte buttonState;
public:
	Button button;

	void initialize(uint8_t pin, int debounceMillis = 10);
	void update(void);

	inline byte getButtonState() {
		return buttonState;
	}

	inline boolean isAutoRepeatEnabled() {
		return autoRepeatEnabled;
	}

	inline boolean setAutoRepeatEnabled(boolean newAutoRepeatEnabled) {
		autoRepeatEnabled = newAutoRepeatEnabled;
	}

	/**
	 * True if double click is detected.
	 * When this method returns true the method isClicked() will ALSO be true.
	 */
	inline boolean isDoubleClicked() {
		return (buttonState == AdvButtonState_DOUBLE_CLICK);
	}

	/**
	 * True if long click is detected.
	 * When this method returns true the method isClicked() will ALSO be true.
	 */
	inline boolean isLongClicked() {
		return (buttonState == AdvButtonState_LONG_CLICK);
	}

	/**
	 * Returns true if buttonState is anything else but AdvButtonState_DOUBLE_CLICK.
	 */
	inline boolean isClicked() {
		return (buttonState != AdvButtonState_NONE);
	}
};

#endif
