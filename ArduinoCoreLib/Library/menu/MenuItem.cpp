#include "Menu.h"

MenuItem::MenuItem(const char *Title, long minValue, long maxValue, boolean looped) :
	title(Title), encoderState(minValue, maxValue, looped) {
}

byte MenuItem::getMenuItemType(void) {
	return MENU_ITEM;
}
