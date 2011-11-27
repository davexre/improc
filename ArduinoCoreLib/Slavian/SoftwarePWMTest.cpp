#include "Arduino.h"
#include "menu/Menu.h"
#include "utils.h"
#include "SoftwarePWM.h"
#include "TicksPerSecond.h"

DefineClass(SoftwarePWMTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin

static AdvButton btn;
static bool speakerOn = true;

static MenuItem frequencyCyclesPerMinuteMenu;
static MenuItem pulseWidthMenu;

static MenuItem *menuItems[] = { &pulseWidthMenu, &frequencyCyclesPerMinuteMenu };

static SimpleMenuWithSerialPrint menu;

static SoftwarePWM spwm;

static TicksPerSecond tps;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;

void SoftwarePWMTest::setup() {
	// Init menus
	frequencyCyclesPerMinuteMenu.initialize("Frequency", 0, 2000, false);
	pulseWidthMenu.initialize("Pulse Width", 0, 255, false);

	diButtonPin.initialize(buttonPin, true);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);

	menu.initialize(&diRotorPinA, &diRotorPinB, &diButtonPin, menuItems, size(menuItems));
	attachInterrupt(0, updateRotaryEncoder, CHANGE);

	diLedPin.initialize(ledPin, 0);
	spwm.initialize(&diLedPin, frequencyCyclesPerMinuteMenu.getValue());
	spwm.setValue(pulseWidthMenu.getValue());

	tps.initialize(500);

    Serial.begin(115200);
    Serial.println("Push the encoder button to switch between menus");
}

void SoftwarePWMTest::loop() {
	tps.update(true);
	spwm.update();
	menu.update();

	if (frequencyCyclesPerMinuteMenu.hasValueChanged()) {
		spwm.setFrequencyCyclesPerMinute(frequencyCyclesPerMinuteMenu.getValue());
	}
	if (pulseWidthMenu.hasValueChanged()) {
		spwm.setValue(pulseWidthMenu.getValue());
	}
	if (menu.button.isLongClicked()) {
		Serial.print("F=");
		Serial.print(spwm.getFrequencyCyclesPerMinute());
		Serial.print(" W=");
		Serial.print((int)spwm.getValue());
		Serial.print(" TPS=");
		Serial.println(tps.getTPS());
	}
}
