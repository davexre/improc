#include "Arduino.h"
#include "Button.h"

DefineClass(DummyTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static Button btn;
static bool lightOn = false;

class DummyA {
public:
	enum MovementMode {
		Idle = 1,
		ContinuousForward = 2,
		ContinuousBackward = 3,
		GotoStep = 4
	};
};


static uint8_t a;
static DummyA::MovementMode mode;

long x,y,d,v;

void DummyTest::setup() {
	sei();
	sei();
	sei();

	a = DummyA::ContinuousBackward;

	sei();
	sei();
	sei();

	mode = DummyA::ContinuousForward;

	sei();
	sei();
	sei();

	pinMode(ledPin, OUTPUT);
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true));
}

void DummyTest::loop() {
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
}
