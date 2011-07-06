#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"
#include "StateLed.h"
#include "TimerOne.h"
#include "DigitalIO.h"

DefineClass(TimerOneTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerUsingTonePin = 8;
static const int speakerUsingTimerOnePin = 9;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static AdvButton btn;
static bool speakerOn = false;
static RotaryEncoderAcelleration rotor;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static RotaryEncoderState toneState;

static void UpdateRotor() {
	rotor.update();
}

static DigitalOutputArduinoPin *speaker;

static void UpdateTimerOne() {
	speaker->setState(speakerOn ? (!speaker->getState()) : false);
}

void TimerOneTest::setup() {
	pinMode(speakerUsingTonePin, OUTPUT);
	speaker = new DigitalOutputArduinoPin(speakerUsingTimerOnePin, false);
	Timer1.initialize();
	Timer1.attachInterrupt(UpdateTimerOne);

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);
	toneState.setValue(500);
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setState(&toneState);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(115200);
    Serial.println("Push the encoder button to toggle sound on/off");

    toneState.initialize(50, 15000, false);
}

void TimerOneTest::loop() {
	btn.update();
	led.update();

	if (btn.isClicked()) {
		speakerOn = !speakerOn;
		if (speakerOn) {
			long newTone = toneState.getValue();
			tone(speakerUsingTonePin, newTone);
			Timer1.startWithFrequency(newTone * 2);
		} else {
			noTone(speakerUsingTonePin);
			Timer1.stop();
		}
	}

	if (toneState.hasValueChanged()) {
		long newTone = toneState.getValue();
		if (speakerOn) {
			tone(speakerUsingTonePin, newTone);
			Timer1.startWithFrequency(newTone * 2);
		}
		float tps = rotor.tps.getTPS();
		Serial.print("Tone ");
		Serial.print(newTone);
		Serial.print(" ");
		Serial.println(tps);
	}
}
