#include "Menu.h"

void MenuItem::initialize(const char *Title, const long minValue, const long maxValue, const bool looped) {
	title = Title;
	encoderState.initialize();
	encoderState.setMinMax(minValue, maxValue);
	encoderState.setValueLooped(looped);
	encoderState.setValue(minValue);
}

byte MenuItem::getMenuItemType(void) {
	return MENU_ITEM;
}
