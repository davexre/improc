#include "Arduino.h"
#include "DigitalIO.h"
#include "reprap/StepperAxis.h"
#include "menu/Menu.h"
#include "StateLed.h"

DefineClass(StepperAxisTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static const int endPositionButtonPin = 5;
static const int stepMotor11pin = 8;
static const int stepMotor12pin = 9;
static const int stepMotor21pin = 10;
static const int stepMotor22pin = 11;

StepperAxis axis;

static const char *axisMenuItems[] = { "Determine available steps", "Initialize to zero position", "Stop" };
static MenuItemEnum axisMenu;

static MenuItem *menuItems[] = { &axisMenu };
static SimpleMenuWithSerialPrint menu;
static StateLed led;

static const unsigned int *ledStates[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

void StepperAxisTest::setup() {
	led.initialize(new DigitalOutputArduinoPin(ledPin), ledStates, size(ledStates), true);
	axis.initialize(
			new DigitalInputArduinoPin(endPositionButtonPin, true),
			new DigitalOutputArduinoPin(stepMotor11pin, 0),
			new DigitalOutputArduinoPin(stepMotor12pin, 0),
			new DigitalOutputArduinoPin(stepMotor21pin, 0),
			new DigitalOutputArduinoPin(stepMotor22pin, 0));

	axisMenu.initialize("Axis", axisMenuItems, size(axisMenuItems), false);
	menu.initialize(new DigitalInputArduinoPin(rotorPinA, true), new DigitalInputArduinoPin(rotorPinB, true),
			new DigitalInputArduinoPin(buttonPin, true), menuItems, size(menuItems));
	Serial.begin(115200);
    Serial.println("Long click the encoder button to select method.");
	attachInterrupt(0, updateRotaryEncoder, CHANGE);
}

static byte mode = 0;
static byte modeState = 0;

void doDetermineAvailableSteps() {
	switch (modeState) {
	case 0:
		axis.determineAvailableSteps();
		Serial.println("Started determineAvailableSteps()");
		modeState = 1;
		break;
	case 1:
		if (axis.getMode() == StepperAxisModeIdle) {
			Serial.print("MaxStep=");
			Serial.println(axis.getMaxStep());
			mode = 0;
		}
		break;
	}
}

void doInitializeToStartingPosition() {
	switch (modeState) {
	case 0:
		axis.initializeToStartingPosition();
		Serial.println("Started initializeToStartingPosition()");
		modeState = 1;
		break;
	case 1:
		if ((axis.getMode() == StepperAxisModeIdle) ||
			(axis.getMode() == StepperAxisModeError)) {
			Serial.println("Done");
			mode = 0;
		}
		break;
	}
}

boolean dummy = false;
void StepperAxisTest::loop() {
	led.update();
	axis.update();
	menu.update();

	int selectedMenu = (int) axisMenu.getValue();
	if (menu.button.isLongClicked()) {
		if ((mode == 0) || (selectedMenu == 2)) {
			mode = selectedMenu + 1;
			modeState = 0;
		}
	}

	switch (mode) {
	case 0:
		break;
	case 1:
		doDetermineAvailableSteps();
		break;
	case 2:
		doInitializeToStartingPosition();
		break;
	case 3:
//		axis.stop();
		axis.motor.rotate(dummy);
		dummy != dummy;
		mode = 0;
		break;
	}
}
