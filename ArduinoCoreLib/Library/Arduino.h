#ifndef ARDUINO_H_
#define ARDUINO_H_

#include <WProgram.h>
#include <utils.h>

#define DefineClass(className) \
class className { \
public: \
	static void setup(); \
	static void loop(); \
}

#endif
