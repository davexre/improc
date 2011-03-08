#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"
#include "StateLed.h"

DefineClass(MenuLikeTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static AdvButton btn;
static boolean speakerOn = true;
static RotaryEncoderAcelleration rotor;
static StateLed led;

class MenuItem {
public:
	const char *title;
	RotaryEncoderState encoderState;

	MenuItem(const char *Title, long minValue, long maxValue, boolean looped = false) :
		title(Title), encoderState(minValue, maxValue, looped) {
	}
};

class MenuItemEnum : public MenuItem {
public:
	const char **items;

	MenuItemEnum(const char *Title, const char **Items, unsigned int ItemsCount, boolean looped = true) :
		MenuItem(Title, 0, ItemsCount, looped), items(Items) {
	}
};

class MenuList : public MenuItem {
public:
	const MenuItem *menuItems;

	MenuList(const char *Title, const MenuItem* MenuItems, unsigned int ItemsCount, boolean looped = true) :
		MenuItem(Title, 0, ItemsCount, looped), menuItems(MenuItems) {
	}
};

static MenuItem simpleValue1 = MenuItem("simple value 1", 0, 10, false);
static MenuItem simpleValue2 = MenuItem("simple value 2", -50, 50, true);

static const char *enumItems[] = {
	"a1", "a2", "a3"
};

static const char *enumItemsOnOff[] = {
	"On", "Off"
};

static MenuItemEnum menuEnum1 = MenuItemEnum("enum", enumItems, size(enumItems));
static MenuItemEnum menuEnumOnOff = MenuItemEnum("on/off", enumItemsOnOff, size(enumItemsOnOff));

static const MenuItem menuListItems[] = { simpleValue1, simpleValue2, menuEnum1, menuEnumOnOff };
static MenuList menuList = MenuList("Main menu", menuListItems, size(menuListItems));

static void menuInitialize() {
	btn.initialize(buttonPin, false);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setState(&menuList.encoderState);
}

static void UpdateRotor() {
	rotor.update();
}

static void menuUpdate() {
	btn.update();
	if (btn.isClicked()) {

	}
}




static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static RotaryEncoderState ledState = RotaryEncoderState(0, size(states), true);
static RotaryEncoderState toneState = RotaryEncoderState(50, 5000, false);


void MenuLikeTest::setup() {
	pinMode(speakerPin, OUTPUT);
	btn.initialize(buttonPin, false);
	led.initialize(ledPin, size(states), states, true);
	toneState.setValue(500);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setState(&toneState);
	attachInterrupt(0, UpdateRotor, CHANGE);
    Serial.begin(9600);
    Serial.println("Push the encoder button to switch between changing pitch and blink");
}

void MenuLikeTest::loop() {
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
