#ifndef Menu_h
#define Menu_h

#include "AdvButton.h"
#include "RotaryEncoderAcelleration.h"

enum MenuItemType {
	MENU_ITEM, MENU_ITEM_ENUM, MENU_LIST
};

class MenuItem {
public:
	const char *title;
	RotaryEncoderState encoderState;

	MenuItem(const char *Title, long minValue, long maxValue, boolean looped = false);

	/**
	 * Returns true if the value has changed since the last call to getValue().
	 */
	inline boolean hasValueChanged() {
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

	MenuItemEnum(const char *Title, const char **Items, unsigned int ItemsCount, boolean looped = true);

	virtual byte getMenuItemType(void);
};

class MenuList : public MenuItem {
public:
	const MenuItem *menuItems;

	MenuList(const char *Title, const MenuItem* MenuItems, unsigned int ItemsCount, boolean looped = true);

	virtual byte getMenuItemType(void);
};

class SimpleMenu {
protected:
	MenuItem **menuItems;
	short int itemsCount;
	short int currentMenu;
	boolean _hasMenuChanged;
public:
	RotaryEncoderAcelleration rotor;
	AdvButton button;

	void initialize(uint8_t encoderPinA, uint8_t encoderPinB, uint8_t buttonPin,
			MenuItem **MenuItems, short int ItemsCount);

	void update();

	inline void updateRotaryEncoder(void) {
		rotor.update();
	}

	void activateMenuItem(short int menuItem);

	inline void activateNextMenuItem() {
		activateMenuItem(currentMenu + 1);
	}

	inline void activatePreviousMenuItem() {
		activateMenuItem(currentMenu - 1);
	}

	inline boolean hasMenuChanged() {
		return _hasMenuChanged;
	}

	/**
	 * Returns true if a menu has changed OR any of the
	 * #MenuItems.hasValueChanged()# returns true.
	 */
	boolean hasChanged();
};

class SimpleMenuWithSerialPrint : public SimpleMenu {
public:
	void update();
};

#endif
