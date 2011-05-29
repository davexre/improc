#include "Arduino.h"
#include "BlinkingLed.h"
#include "Button.h"

DefineClass(BlinkingLedTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 13;		// the number of the LED pin

static Button btn;
static BlinkingLed led;

void BlinkingLedTest::setup(void) {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0));
}

void BlinkingLedTest::loop(void) {
	btn.update();
	led.update();
	if (btn.isPressed()) {
		led.playBlink(BLINK2, 1);
	}
}
