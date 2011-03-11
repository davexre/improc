#include "DummyTest.h"

DummyTest dt;

extern "C" int main2(void) {
	dt.method1();
	dt.method1();
	dt.method1();
	return 0;
}
