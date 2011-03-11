#include "Menu.h"

void MenuList::initialize(const char *Title, const MenuItem* MenuItems, unsigned int ItemsCount, boolean looped) {
	menuItems = MenuItems;
	MenuItem::initialize(Title, 0, ItemsCount, looped);
}

byte MenuList::getMenuItemType(void) {
	return MENU_LIST;
}
