#include "Menu.h"
#include "HardwareSerial.h"

void SimpleMenuWithSerialPrint::update(void) {
	SimpleMenu::update();
	if (hasChanged()) {
		MenuItem *cur = menuItems[currentMenu];
		long curValue = cur->encoderState.peekValue();
		Serial.print(cur->title);
		Serial.print(": ");
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
