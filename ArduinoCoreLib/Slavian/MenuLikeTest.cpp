#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"
#include "StateLed.h"
#include "menu/Menu.h"

DefineClass(MenuLikeTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static const char *speakerStates[] = { "ON", "OFF" };

static const unsigned int *ledStates[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static MenuItemEnum speakerMenu = MenuItemEnum("Speaker", speakerStates, size(speakerStates), false);
static MenuItem ledStatesMenu = MenuItem("Led state", 0, size(ledStates) - 1, true);
static MenuItem tonePitchMenu = MenuItem("Pitch", 50, 5000, false);

static MenuItem menuItems[] { speakerMenu, ledStatesMenu, tonePitchMenu }

static SimpleMenu menu;
static StateLed led;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}



//static boolean speakerOn = true;
//static AdvButton btn;
//static RotaryEncoderAcelleration rotor;
//
//static MenuItem simpleValue3 = MenuItem("simple value 3", 0, 100, true);
//
//static const char *enumItems[] = {
//	"a1", "a2", "a3"
//};
//
//static const char *enumItemsOnOff[] = {
//	"On", "Off"
//};
//
//static MenuItemEnum menuEnum1 = MenuItemEnum("enum", enumItems, size(enumItems));
//static MenuItemEnum menuEnumOnOff = MenuItemEnum("on/off", enumItemsOnOff, size(enumItemsOnOff));
//static MenuItemEnum menuEnumOnOff2 = MenuItemEnum("on/off2", enumItemsOnOff, size(enumItemsOnOff));
//static MenuItem menuListItems[] = { simpleValue1, simpleValue2, simpleValue3 };
//static MenuList menuList = MenuList("Main menu", menuListItems, size(menuListItems));


void MenuLikeTest::setup() {
	pinMode(speakerPin, OUTPUT);
	led.initialize(ledPin, size(states), states, true);
	menu.initialize(rotorPinA, rotorPinB, buttonPin, menuItems, size(menuItems));
	attachInterrupt(0, updateRotaryEncoder, CHANGE);
    Serial.begin(9600);
    Serial.println("Push the encoder button to switch between menus");
}

void MenuLikeTest::loop() {
	led.update();
	menu.update();

	if (ledStatesMenu.hasValueChanged()) {
		led.setState(ledStatesMenu.getValue());
	}
	if (speakerMenu.hasValueChanged()) {
		if (speakerMenu.getValue() == 0) {
			tone
		}
	}

	if (menu.hasChanged()) {
	}

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
