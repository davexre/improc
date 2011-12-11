#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "reprap/RepRapPCB2.h"
#include "SerialReader.h"

DefineClass(MemoryTest);
/*
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static AdvButton btn;
static StateLed led;

static char readerBuffer[100];
static SerialReader reader;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;
*/
void MemoryTest::setup() {
/*	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);
*/
    Serial.println("Initialized123456");
//	Serial.println("abcde");
//    Serial.println("Press the button to stop");
}

void MemoryTest::loop() {
/*	reader.update();
	btn.update();
	led.update();

	if (reader.available()) {
	}
	if (btn.isLongClicked()) {
	} else if (btn.isClicked()) {
	}*/
}
