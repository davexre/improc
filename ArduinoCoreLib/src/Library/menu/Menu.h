#ifndef Menu_h
#define Menu_h

#include "DigitalIO.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"

enum MenuItemType {
	MENU_ITEM, MENU_ITEM_ENUM, MENU_LIST
};

class MenuItem {
public:
	const char *title;
	RotaryEncoderState encoderState;

	void initialize(const char *Title, const long minValue, const long maxValue, const bool looped = false) {
		title = Title;
		encoderState.initialize();
		encoderState.setMinMax(minValue, maxValue);
		encoderState.setValueLooped(looped);
		encoderState.setValue(minValue);
	}

	/**
	 * Returns true if the value has changed since the last call to getValue().
	 */
	inline bool hasValueChanged() {
		return encoderState.hasValueChanged();
	}

	/**
	 * Gets the #value# of the encoder.
	 */
	inline long getValue() {
		return encoderState.getValue();
	}

	inline void setValue(const long newValue) {
		encoderState.setValue(newValue);
	}

	virtual byte getMenuItemType(void) {
		return MENU_ITEM;
	}
};

class MenuItemEnum : public MenuItem {
public:
	const char **items;

	void initialize(const char *Title, const char **Items, const unsigned int ItemsCount, const bool looped = true) {
		items = Items;
		MenuItem::initialize(Title, 0, ItemsCount - 1, looped);
	}

	virtual byte getMenuItemType(void) {
		return MENU_ITEM_ENUM;
	}
};

class MenuList : public MenuItem {
public:
	const MenuItem *menuItems;

	void initialize(const char *Title, const MenuItem* MenuItems, const unsigned int ItemsCount, const bool looped = true) {
		menuItems = MenuItems;
		MenuItem::initialize(Title, 0, ItemsCount, looped);
	}

	virtual byte getMenuItemType(void) {
		return MENU_LIST;
	}
};

class SimpleMenu {
protected:
	MenuItem **menuItems;
	short int itemsCount;
	short int currentMenu;
	bool _hasMenuChanged;
	RotaryEncoderAcceleration rotor;
	RotaryEncoderState *switchMenuEncoderState;
	bool wasMenuSwitched;

	void activateMenuItem(short int menuItem, bool setRotor) {
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
public:
	AdvButton button;

	void initialize(DigitalInputPin *encoderPinA, DigitalInputPin *encoderPinB, DigitalInputPin *buttonPin,
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

	void update() {
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

	inline void updateRotaryEncoder(void) {
		rotor.update();
	}

	inline void activateMenuItem(short int menuItem) {
		activateMenuItem(menuItem, true);
	}

	inline void activateNextMenuItem() {
		activateMenuItem(currentMenu + 1, true);
	}

	inline void activatePreviousMenuItem() {
		activateMenuItem(currentMenu - 1, true);
	}

	inline short int getCurrentMenu() {
		return currentMenu;
	}

	inline short int getItemsCount() {
		return itemsCount;
	}

	/**
	 * Returns true if a menu has changed OR any of the
	 * #MenuItems.hasValueChanged()# returns true.
	 */
	bool hasChanged() {
		if (_hasMenuChanged)
			return true;
		for (int i = itemsCount - 1; i >= 0; i--)
			if (menuItems[i]->hasValueChanged())
				return true;
		return false;
	}
};

class SimpleMenuWithSerialPrint : public SimpleMenu {
public:
	void update() {
		SimpleMenu::update();
		if (hasChanged()) {
			MenuItem *cur = menuItems[currentMenu];
			long curValue = cur->encoderState.peekValue();
			Serial.print(cur->title);
			Serial_print(": ");
			switch (cur->getMenuItemType()) {
			case MENU_ITEM_ENUM:
				Serial.println(((MenuItemEnum*)cur)->items[curValue]);
				break;
			case MENU_ITEM:
			default:
				Serial.println(curValue);
				break;
			}
		}
	}
};

#endif
