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
	button.update();
	if (button.getButtonState() == AdvButtonState_CLICK) {
		activateNextMenuItem();
	} else if (button.getButtonState() == AdvButtonState_DOUBLE_CLICK) {
		// Every a double click is preceeded by a single click and this
		// means that the activateNextMenuItem is already invoked but
		// a double click was ment instead.
		activatePreviousMenuItem();
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

bool SimpleMenu::hasChanged() {
	if (_hasMenuChanged)
		return true;
	for (int i = itemsCount - 1; i >= 0; i--)
		if (menuItems[i]->hasValueChanged())
			return true;
	return false;
}
