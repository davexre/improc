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
static boolean speakerOn = false;

void PWM_Led::setup() {
	pinMode(ledPin, OUTPUT);
	pinMode(speakerPin, OUTPUT);

	btn.initialize(buttonPin);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setMinMax(0, 255);
	rotor.setPosition(1);

    Serial.begin(9600);
}

static long lastPWM = -1;

void PWM_Led::loop() {
	btn.update();
	rotor.update();

	int pwm = (int) rotor.getPosition();

	if (btn.isPressed()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			lastPWM = -1;
		} else {
			digitalWrite(ledPin, 0);
			noTone(speakerPin);
		}
	}

	if (speakerOn) {
		if (pwm != lastPWM) {
			unsigned int pitch = 10 + (pwm << 1);
			tone(speakerPin, pitch, 0);
			analogWrite(ledPin, pwm);
			lastPWM = pwm;
			Serial.print(pwm);
			Serial.print("  ");
			Serial.println(pitch);
		}
	}
}
