#include "WProgram.h"

int main_NO(void)
{
	init();

	setup();
    
	for (;;)
		loop();
        
	return 0;
}

