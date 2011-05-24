#include "Menu.h"

void SimpleMenu::initialize(const uint8_t encoderPinA, const uint8_t encoderPinB, const uint8_t buttonPin,
		MenuItem **MenuItems, const short int ItemsCount) {
	button.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	rotor.initialize(
			new DigitalInputArduinoPin(encoderPinA, true),
			new DigitalInputArduinoPin(encoderPinB, true));
	menuItems = MenuItems;
	itemsCount = ItemsCount;
	activateMenuItem(0);
}

void SimpleMenu::update(void) {
	button.update();
	if (button.getButtonState() == AdvButtonState_CLICK) {
		activateNextMenuItem();
	} else {
		_hasMenuChanged = false;
	}
}

void SimpleMenu::activateMenuItem(short int menuItem) {
	if (menuItem >= itemsCount)
		menuItem %= itemsCount;
	if (menuItem < 0)
		menuItem = itemsCount - 1 + menuItem % itemsCount;
	_hasMenuChanged = true;
	currentMenu = menuItem;
	rotor.setState(&menuItems[currentMenu]->encoderState);
}

boolean SimpleMenu::hasChanged() {
	if (hasMenuChanged())
		return true;
	for (int i = itemsCount - 1; i >= 0; i--)
		if (menuItems[i]->hasValueChanged())
			return true;
	return false;
}
