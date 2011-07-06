#include "Menu.h"

void MenuItemEnum::initialize(const char *Title, const char **Items, const unsigned int ItemsCount, const bool looped) {
	items = Items;
	MenuItem::initialize(Title, 0, ItemsCount - 1, looped);
}

byte MenuItemEnum::getMenuItemType(void) {
	return MENU_ITEM_ENUM;
}
