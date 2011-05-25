#include "Menu.h"

void SimpleMenu::initialize(DigitalInputPin *encoderPinA, DigitalInputPin *encoderPinB, DigitalInputPin *buttonPin,
		MenuItem **MenuItems, const short int ItemsCount) {
	button.initialize(buttonPin, false);
	rotor.initialize(encoderPinA, encoderPinB);
	menuItems = MenuItems;
	itemsCount = ItemsCount;
	activateMenuItem(0);
}

void SimpleMenu::update(void) {
	_hasMenuChanged = false;
	button.update();
	if (button.getButtonState() == AdvButtonState_CLICK) {
		activateNextMenuItem();
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
