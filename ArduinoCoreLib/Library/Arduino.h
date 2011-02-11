#ifndef ARDUINO_H_
#define ARDUINO_H_

#include <WProgram.h>

#define size(arr) sizeof(arr) / sizeof(arr[0])
#define disableInterrupts() uint8_t oldSREG = SREG; cli()
#define restoreInterrupts() SREG = oldSREG

#define DefineClass(className) \
class className { \
public: \
	static void setup(); \
	static void loop(); \
}

#endif
