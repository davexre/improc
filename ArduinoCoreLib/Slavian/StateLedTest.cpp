#include "Arduino.h"
#include "utils.h"
#include "StateLed.h"
#include "Button.h"

DefineClass(StateLedTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3,
		BLINK_ON, BLINK_OFF
};

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;

void StateLedTest::setup(void) {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);
}

void StateLedTest::loop(void) {
	btn.update();
	led.update();
	if (btn.isPressed())
		led.nextState();
}
