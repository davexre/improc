#include "Arduino.h"

extern "C" void __cxa_pure_virtual(void);
__extension__ typedef int __guard __attribute__((mode (__DI__)));
extern "C" int __cxa_guard_acquire(__guard *);
extern "C" void __cxa_guard_release (__guard *);
extern "C" void __cxa_guard_abort (__guard *);

void __cxa_pure_virtual(void) {};
int __cxa_guard_acquire(__guard *g) {return !*(char *)(g);};
void __cxa_guard_release (__guard *g) {*(char *)g = 1;};
void __cxa_guard_abort (__guard *) {};

#define MAINCLASS AnalogSensorTest

DefineClass(MAINCLASS);
MAINCLASS mainClass;

extern "C" int main(void)
{
	init();

	mainClass.setup();
    
	for (;;)
		mainClass.loop();
        
	return 0;
}
