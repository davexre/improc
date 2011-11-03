#include "Arduino.h"
#include "utils.h"
#include "Button.h"

DefineClass(OpticalSensorTest);

static const int buttonPin = 4;		// the number of the pushbutton pin

static Button btn;

static unsigned long last;

void OpticalSensorTest::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
    Serial.begin(115200);
    last = millis();
}

void OpticalSensorTest::loop() {
	btn.update();

	if ((millis() - last > 500) || btn.isPressed()) {
		last = millis();
		Serial.print(analogRead(0));
//		Serial.print("\t");
//		Serial.print(analogRead(5));
		Serial.print("\n");
	}
}
