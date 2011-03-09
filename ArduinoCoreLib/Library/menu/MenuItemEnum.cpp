#include "Menu.h"

MenuItemEnum::MenuItemEnum(const char *Title, const char **Items, unsigned int ItemsCount, boolean looped) :
	MenuItem(Title, 0, ItemsCount, looped), items(Items) {
}

byte MenuItemEnum::getMenuItemType(void) {
	return MENU_ITEM_ENUM;
}
