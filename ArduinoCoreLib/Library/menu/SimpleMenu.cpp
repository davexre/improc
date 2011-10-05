#include "Menu.h"

void SimpleMenu::initialize(DigitalInputPin *encoderPinA, DigitalInputPin *encoderPinB, DigitalInputPin *buttonPin,
		MenuItem **MenuItems, const short int ItemsCount) {
	button.initialize(buttonPin, false);
	rotor.initialize(encoderPinA, encoderPinB);
	switchMenuEncoderState = rotor.getState();
	switchMenuEncoderState->setValueLooped(false);
	switchMenuEncoderState->setMinMax(0, ItemsCount - 1);
	menuItems = MenuItems;
	itemsCount = ItemsCount;
	wasMenuSwitched = false;
	activateMenuItem(0, true);
}

void SimpleMenu::update(void) {
	_hasMenuChanged = false;
	button.update();
	if (switchMenuEncoderState->hasValueChanged()) {
		wasMenuSwitched = true;
		activateMenuItem(switchMenuEncoderState->getValue(), false);
	}
	if (button.isButtonToggled()) {
		if (button.isButtonDown()) {
			rotor.setState(switchMenuEncoderState);
		} else {
			rotor.setState(&menuItems[currentMenu]->encoderState);
			if (wasMenuSwitched) {
				button.reset();
			}
		}
		wasMenuSwitched = false;
	} else if (wasMenuSwitched) {
		button.reset();
	}
}

void SimpleMenu::activateMenuItem(short int menuItem, bool setRotor) {
	if (menuItem >= itemsCount)
		menuItem %= itemsCount;
	if (menuItem < 0)
		menuItem = itemsCount - 1 + menuItem % itemsCount;
	if (currentMenu != menuItem) {
		_hasMenuChanged = true;
		currentMenu = menuItem;
	}
	if (setRotor)
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
