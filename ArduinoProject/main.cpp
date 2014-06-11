#include "Arduino.h"
#include "utils.h"
#include "Test1.h"
#include "examples/ShiftRegisterInputTest.h"

ShiftRegisterInputTest<> mainClass;

extern "C" int main (void)
{
	init();

	mainClass.initialize();
	for (;;)
		mainClass.update();


	return 0;
}
