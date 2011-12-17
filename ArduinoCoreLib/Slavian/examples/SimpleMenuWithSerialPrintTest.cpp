#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"
#include "StateLed.h"
#include "menu/Menu.h"

DefineClass(SimpleMenuWithSerialPrintTest);

static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin

static const char *speakerStates[] = { "ON", "OFF" };

static const unsigned int PROGMEM *ledStates[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static MenuItemEnum speakerMenu;
static MenuItem ledStatesMenu;
static MenuItem tonePitchMenu;

static MenuItem *menuItems[] = { &speakerMenu, &ledStatesMenu, &tonePitchMenu };

static SimpleMenuWithSerialPrint menu;
static StateLed led;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

void SimpleMenuWithSerialPrintTest::setup() {
	pinMode(speakerPin, OUTPUT);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, ledStates, size(ledStates), true);

	// Init menus
	speakerMenu.initialize("Speaker", speakerStates, size(speakerStates), false);
	ledStatesMenu.initialize("Led state", 0, size(ledStates) - 1, true);
	tonePitchMenu.initialize("Pitch", 50, 5000, false);

	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	diButtonPin.initialize(buttonPin, true);
	menu.initialize(&diRotorPinA, &diRotorPinB, &diButtonPin, menuItems, size(menuItems));
	attachInterrupt(0, updateRotaryEncoder, CHANGE);
    Serial.begin(115200);
    Serial.println("Push the encoder button to switch between menus");
}

void SimpleMenuWithSerialPrintTest::loop() {
	led.update();
	menu.update();

	if (ledStatesMenu.hasValueChanged()) {
//		Serial.println("LED CHANGED");
		led.setState(ledStatesMenu.getValue());
	}
	if (tonePitchMenu.hasValueChanged() || speakerMenu.hasValueChanged()) {
		long pitch = tonePitchMenu.getValue();
		if (speakerMenu.getValue() == 0) {
//			Serial.println("TONE");
			tone(speakerPin, pitch);
		} else {
//			Serial.println("NO TONE");
			noTone(speakerPin);
		}
	}

	if (menu.button.isLongClicked()) {
		Serial.println("LONG CLICK");
	} else if (menu.button.isDoubleClicked()) {
		Serial.println("DOUBLE CLICK");
	} else if (menu.button.isClicked()) {
		Serial.println("CLICK");
	}

}
