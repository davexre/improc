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

	void initialize(const char *Title, const long minValue, const long maxValue, const bool looped = false);

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

	virtual byte getMenuItemType(void);

};

class MenuItemEnum : public MenuItem {
public:
	const char **items;

	void initialize(const char *Title, const char **Items, const unsigned int ItemsCount, const bool looped = true);

	virtual byte getMenuItemType(void);
};

class MenuList : public MenuItem {
public:
	const MenuItem *menuItems;

	void initialize(const char *Title, const MenuItem* MenuItems, const unsigned int ItemsCount, const bool looped = true);

	virtual byte getMenuItemType(void);
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
	void activateMenuItem(short int menuItem, bool setRotor);
public:
	AdvButton button;

	void initialize(DigitalInputPin *encoderPinA, DigitalInputPin *encoderPinB, DigitalInputPin *buttonPin,
			MenuItem **MenuItems, const short int ItemsCount);

	void update();

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
	bool hasChanged();
};

class SimpleMenuWithSerialPrint : public SimpleMenu {
public:
	void update();
};

#endif
