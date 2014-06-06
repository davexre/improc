#include "Arduino.h"
#include "utils.h"
#include "Button.h"

class ButtonTest {
	const int buttonPin = 4; // the number of the pushbutton pin
	const int ledPin = 13; // the number of the LED pin

	Button btn;
	bool lightOn = false;

	DigitalInputArduinoPin diButtonPin;
public:
	void initialize() {
		pinMode(ledPin, OUTPUT);
		diButtonPin.initialize(buttonPin, true);
		btn.initialize(&diButtonPin);
	}

	void update() {
		btn.update();
		if (btn.isPressed()) {
			lightOn = !lightOn;
			digitalWrite(ledPin, lightOn ? HIGH : LOW);
		}
	}
};

