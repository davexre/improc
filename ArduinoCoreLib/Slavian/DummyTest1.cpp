#include "Arduino.h"
#include "DummyTest.h"

void DummyTest::method1() {
	cli();
	cli();
	cli();
}
void DummyTest::method2() {
	sei();
	sei();
	sei();
}
