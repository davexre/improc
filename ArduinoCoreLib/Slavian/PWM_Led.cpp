#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "RotaryEncoderAcelleration.h"

DefineClass(PWM_Led);

static const int buttonPin = 4;		// the number of the pushbutton pin
static const int ledPin = 6;		// the number of the LED pin
static const int speakerPin = 8;
static const int rotorPinA = 2;		// One quadrature pin
static const int rotorPinB = 3;		// the other quadrature pin

static Button btn;
static RotaryEncoderAcelleration rotor;
static bool speakerOn = false;

void PWM_Led::setup() {
	pinMode(ledPin, OUTPUT);
	pinMode(speakerPin, OUTPUT);

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setMinMax(0, 255);
	rotor.setValue(1);

    Serial.begin(115200);
}

static void doPlay(int pwm) {
	unsigned int pitch = 10 + (pwm << 1);
	tone(speakerPin, pitch, 0);
	analogWrite(ledPin, pwm);
}

void PWM_Led::loop() {
	btn.update();
	rotor.update();

	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			doPlay(rotor.getValue());
		} else {
			digitalWrite(ledPin, 0);
			noTone(speakerPin);
		}
	}

	if (speakerOn && rotor.hasValueChanged()) {
		int pwm = (int) rotor.getValue();
		doPlay(pwm);
		Serial.println(pwm);
	}
}
