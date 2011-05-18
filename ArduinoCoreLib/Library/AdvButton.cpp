#include "AdvButton.h"

void AdvButton::initialize(const uint8_t pin, const boolean autoRepeatEnabled, const int debounceMillis) {
	button.initialize(pin, debounceMillis);
	this->autoRepeatEnabled = autoRepeatEnabled;
	timeNextAutorepeatToggle = previousTimeButtonUp = timeButtonDown = 0;
	autoButtonStarted = false;
	buttonState = AdvButtonState_NONE;
}

void AdvButton::update(void) {
	buttonState = AdvButtonState_NONE;
	button.update();
	long now = millis();
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
					buttonState = AdvButtonState_DOUBLE_CLICK;
				} else {
					buttonState = AdvButtonState_CLICK;
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
				buttonState = AdvButtonState_AUTOREPEAT_CLICK;
			}
		} else {
			if ((!autoButtonStarted) && (now - timeButtonDown >= ADV_BUTTON_LONG_CLICK_MILLIES)) {
				autoButtonStarted = true;
				buttonState = AdvButtonState_LONG_CLICK;
			}
		}
	}
}
