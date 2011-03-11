#include "Menu.h"

void MenuItem::initialize(const char *Title, long minValue, long maxValue, boolean looped) {
	title = Title;
	encoderState.initialize();
	encoderState.setMinMax(minValue, maxValue);
	encoderState.setValueLooped(looped);
	encoderState.setValue(minValue);
}

byte MenuItem::getMenuItemType(void) {
	return MENU_ITEM;
}
