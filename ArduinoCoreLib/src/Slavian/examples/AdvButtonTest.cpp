#include <Arduino.h>
#include "utils.h"
#include "AdvButton.h"
#include "StateLed.h"

template<typename dummy=void>
class AdvButtonTest {
	const int buttonPin = 4; // the number of the pushbutton pin
	const int ledPin = 13; // the number of the LED pin
	static const unsigned int *const PROGMEM states[];

	StateLed led;
	AdvButton btn;
	DigitalOutputArduinoPin diLedPin;
	DigitalInputArduinoPin diButtonPin;

public:
	void initialize() {
		diButtonPin.initialize(buttonPin, true);
		btn.initialize(&diButtonPin, false);
		diLedPin.initialize(ledPin);
		led.initialize(&diLedPin, states, size(states), true);
		Serial.begin(115200);

		led.setState(btn.isAutoRepeatEnabled());
		Serial.pgm_println(PSTR(""));
		Serial_println("Double click to toggle [Long clicks]/[Auto repeat clicks]");
	}

	void update() {
		btn.update();
		led.update();

		if (btn.isLongClicked()) {
			Serial.println("Long click");
		} else if (btn.isDoubleClicked()) {
			Serial.println("Double click");
			btn.setAutoRepeatEnabled(!btn.isAutoRepeatEnabled());
			led.setState(btn.isAutoRepeatEnabled());
			Serial.println(btn.isAutoRepeatEnabled() ? "Autorepeat ON" : "Long clicks ON");
		} else if (btn.isClicked()) {
			Serial.println("Click");
		}
	}
};

template<typename dummy>
const unsigned int *const AdvButtonTest<dummy>::states[] = {
	BLINK_SLOW,
	BLINK_FAST
};

DefineClassTemplate(AdvButtonTest)
