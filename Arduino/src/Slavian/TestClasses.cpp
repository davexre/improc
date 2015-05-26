#include <Arduino.h>
#include "utils.h"

#define TYPE1 int
#define TYPE2 int

#define USE_CONSTRUCTOR
#define USE_TEMPLATES

#ifdef USE_TEMPLATES
template <typename dummy=void>
#endif
class SomeClass {
public:
	int var;
#ifdef USE_CONSTRUCTOR
	SomeClass(int kuku) {
#else
	void initialize(int kuku) {
#endif
		var = kuku;
		Serial.print(var);
	}

	virtual void doSomething() {
		Serial.print(var++);
	}
};

#ifdef USE_TEMPLATES
#define SomeClassDef(type) SomeClass<type>
#else
#define SomeClassDef(type) SomeClass
#endif


#ifdef USE_CONSTRUCTOR
SomeClassDef(TYPE1) someClass1 = SomeClassDef(TYPE1)(55);
SomeClassDef(TYPE2) someClass2 = SomeClassDef(TYPE2)(66);
#else
SomeClassDef(TYPE1) someClass1;
SomeClassDef(TYPE2) someClass2;
#endif

DefineClass(TestClasses);

void TestClasses::setup() {
#ifdef USE_CONSTRUCTOR
#else
	someClass1.initialize(55);
	someClass2.initialize(66);
#endif
}

void TestClasses::loop() {
	someClass1.doSomething();
	someClass2.doSomething();
}

