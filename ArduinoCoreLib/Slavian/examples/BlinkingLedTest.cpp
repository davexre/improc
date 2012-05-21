#include "Arduino.h"
#include "utils.h"
#include "BlinkingLed.h"
#include "Button.h"

DefineClass(BlinkingLedTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 13;		// the number of the LED pin

static Button btn;
static BlinkingLed led;

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;

void BlinkingLedTest::setup(void) {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin);
}

void BlinkingLedTest::loop(void) {
	btn.update();
	led.update();
	if (btn.isPressed()) {
		led.playBlink(BLINK2, 1);
	}
}
