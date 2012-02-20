#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"
#include "StateLed.h"

DefineClass(RotaryEncoderAccelerationTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static AdvButton btn;
static bool speakerOn = true;
static RotaryEncoderAcceleration rotor;
static StateLed led;

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static RotaryEncoderState ledState;
static RotaryEncoderState toneState;

static void UpdateRotor() {
	rotor.update();
}

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

void RotaryEncoderAccelerationTest::setup() {
	pinMode(speakerPin, OUTPUT);
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	rotor.setState(&toneState);
	attachInterrupt(0, UpdateRotor, CHANGE);
	toneState.setValue(500);
    Serial.begin(115200);
    Serial.println("Push the encoder button to switch between changing pitch and blink");

    ledState.initialize(0, size(states) - 1, true);
    toneState.initialize(50, 15000, false);
}

void RotaryEncoderAccelerationTest::loop() {
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			tone(speakerPin, toneState.getValue());
		} else {
			noTone(speakerPin);
		}
	} else if (btn.isClicked()) {
		rotor.setState(rotor.getState() == &toneState ? &ledState : &toneState);
	}

	if (toneState.hasValueChanged()) {
		long newTone = toneState.getValue();
		if (speakerOn) {
			tone(speakerPin, newTone);
		}
		float tps = rotor.tps.getTPS();
		Serial.print("Tone ");
		Serial.print(newTone);
		Serial.print(" ");
		Serial.println(tps);
	}

	if (ledState.hasValueChanged()) {
		int newLed = (int) ledState.getValue();
		led.setState(newLed);
		Serial.print("Led ");
		Serial.println(newLed);
	}
}