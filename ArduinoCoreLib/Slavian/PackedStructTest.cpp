#include "Arduino.h"
#include "Button.h"

DefineClass(PackedStructTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static bool lightOn = false;

class PackedStructA {
public:
	enum MovementMode {
		Idle = 1,
		ContinuousForward = 2,
		ContinuousBackward = 3,
		GotoStep = 4
	};

	struct PackedStruct {
		bool someBool : 1;
		//unsigned int someInt : 7;
		MovementMode someMode : 7;
	};
};

uint8_t qq;
static PackedStructA::PackedStruct a;
static DigitalInputArduinoPin diButtonPin;

uint8_t b;

void PackedStructTest::setup() {
	sei();
	sei();
	sei();

	a.someBool = true;
	b |= 1;

	sei();
	b++;
	sei();
	b = 12;
	sei();

	a.someMode = PackedStructA::ContinuousBackward;
	sei();
	b = 2 << 1 | b & 1;
	sei();
	sei();

	qq = a.someMode;
	sei();
	qq = b >> 2;

	sei();
	sei();

	pinMode(ledPin, OUTPUT);
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin);
}

void PackedStructTest::loop() {
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
}
