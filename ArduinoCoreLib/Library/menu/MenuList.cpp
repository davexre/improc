#include "Menu.h"

MenuList::MenuList(const char *Title, const MenuItem* MenuItems, unsigned int ItemsCount, boolean looped) :
	MenuItem(Title, 0, ItemsCount, looped), menuItems(MenuItems) {
}

byte MenuList::getMenuItemType(void) {
	return MENU_LIST;
}
