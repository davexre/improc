#include "Arduino.h"
#include <pins_arduino.h>
#include "utils.h"
#include "Button.h"
#include "RotaryEncoderAcceleration.h"
#include "DigitalIO.h"
#include "TimerOne.h"

DefineClass(TPU);

//#define USE_ARRAYS

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int ledPin =  13;      // the number of the LED pin
static const int speakerPin = 8;

static const int coilPins[] = { 5, 6, 7 };
static const int coilCount = size(coilPins);

static const byte coilStates[][coilCount] = {
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
};

static const byte coilStates2[][coilCount] = {
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 1, 1},
		{0, 0, 1},
		{1, 0, 1}
};

static const byte coilStates3[][coilCount] = {
		{1, 0, 0},
		{0, 0, 0},
		{0, 1, 0},
		{0, 0, 0},
		{0, 0, 1},
		{0, 0, 0}
};


static const int coilStatesCount = size(coilStates);
static int activeCoilState = 0;

static DigitalOutputArduinoPin coilPorts[coilCount];
static DigitalOutputArduinoPin speakerPort;
static DigitalOutputArduinoPin led;

static Button btn;
static RotaryEncoderAcceleration rotor;

static void UpdateOnTimer1() {
	const byte *states = coilStates[activeCoilState++];
	DigitalOutputArduinoPin *pd = coilPorts;
	for (int i = 0; i < coilCount; i++, pd++) {
	    // set the pin
		pd->setState(*(states++));
	}
	if (activeCoilState >= coilStatesCount) {
		activeCoilState = 0;
		speakerPort.setState(!speakerPort.getState());
	}
}

/////////////////////////////////

static void UpdateRotor(void) {
	rotor.update();
}

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

void TPU::setup() {
	for (int i = 0; i < coilCount; i++) {
		coilPorts[i].initialize(coilPins[i], false);
	}
	speakerPort.initialize(speakerPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(ledPin, false);
	Timer1.initialize();
	Timer1.attachInterrupt(UpdateOnTimer1);

	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	rotor.setMinMax(50, 50000);
	rotor.setValue(100);
	attachInterrupt(0, UpdateRotor, CHANGE);
	Serial.begin(115200);
}

static long curValue = 0;
static long lastValue = -1;

void TPU::loop() {
	btn.update();

	if (btn.isPressed()) {
		led.setState(!led.getState());

		long curValue = rotor.getValue();
		Timer1.startWithFrequency(curValue);
		Serial.println(curValue);
	}

	if (led.getState()) {
		if (rotor.hasValueChanged()) {
			long curValue = rotor.getValue();
			Timer1.startWithFrequency(curValue);
			Serial.println(curValue);
		}
	} else {
		Timer1.stop();
		sei();
#ifdef USE_ARRAYS
		for (int i = 0; i < coilCount; i++) {
			coilPorts[i]->setState(false);
		}
#else
		DigitalOutputArduinoPin *pd = coilPorts;
		for (int i = 0; i < coilCount; i++, pd++) {
			pd->setState(false);
		}
#endif
		activeCoilState = 0;
	}
}
