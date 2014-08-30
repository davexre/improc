#include "Arduino.h"
#include "utils.h"
#include "BlinkingLed.h"
#include "Button.h"

class BlinkingLedTest {
	static const int buttonPin = 4;		// the number of the pushbutton pin
	static const int ledPin = 13;		// the number of the LED pin

	Button btn;
	BlinkingLed led;

	DigitalOutputArduinoPin diLedPin;
	DigitalInputArduinoPin diButtonPin;

	void initialize(void) {
		diButtonPin.initialize(buttonPin, true);
		btn.initialize(&diButtonPin);
		diLedPin.initialize(ledPin, 0);
		led.initialize(&diLedPin);
	}

	void update(void) {
		btn.update();
		led.update();
		if (btn.isPressed()) {
			led.playBlink(BLINK2, 1);
		}
	}
};
