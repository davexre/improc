#include "Arduino.h"
#include <pins_arduino.h>
#include "utils.h"
#include "Button.h"
#include "RotaryEncoderAcelleration.h"

DefineClass(ToneTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int ledPin =  13;      // the number of the LED pin
static const int speakerPin = 8;

static const int coilPins[] = { 5, 6, 7 };
static const int coilCount = size(coilPins);

static const byte coilStates1[][coilCount] = {
		{1, 0, 0},
		{0, 1, 0},
		{0, 0, 1}
};

static const byte coilStates[][coilCount] = {
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 1, 1},
		{0, 0, 1},
		{1, 0, 1}
};

static const byte coilStates3[][coilCount] = {
		{1, 0, 0},
		{0, 0, 0},
		{0, 1, 0},
		{0, 0, 0},
		{0, 0, 1},
		{0, 0, 0}
};


static const int coilStatesCount = size(coilStates);
static int activeCoilState = 0;

static struct pinDesc {
	volatile uint8_t *port;
	volatile uint8_t mask;
} coilPorts[coilCount], speakerPort;

static volatile uint8_t *coilPinPorts[coilCount];
static volatile uint8_t coilPinMaks[coilCount];

static Button btn;
static RotaryEncoderAcelleration rotor;

// frequency in Hertz
static void playTimer1(uint16_t frequency) {
	// two choices for the 16 bit timers: ck/1 or ck/64
	uint32_t ocr = F_CPU / frequency / 2 - 1;
	uint8_t prescalarbits = 0b001;
	if (ocr > 0xffff) {
		ocr = F_CPU / frequency / 2 / 64 - 1;
		prescalarbits = 0b011;
	}
	TCCR1B = (TCCR1B & 0b11111000) | prescalarbits;
    OCR1A = ocr;
    // then turn on the interrupts
    bitWrite(TIMSK1, OCIE1A, 1);
}


static void stopTimer1() {
	TIMSK1 &= ~(1 << OCIE1A);
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++, pd++) {
		*pd->port &= ~(pd->mask);
	}
}

static bool isPlaying(void) {
  return TIMSK1 & (1 << OCIE1A);
}

ISR(TIMER1_COMPA_vect) {
	const byte *states = coilStates[activeCoilState++];
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++, pd++) {
	    // set the pin
		if (*(states++)) {
			// set bit
			*pd->port |= pd->mask;
		} else {
			// clear bit
			*pd->port &= ~(pd->mask);
		}
	}
	if (activeCoilState >= coilStatesCount) {
		activeCoilState = 0;
		*speakerPort.port ^= speakerPort.mask;
	}
}

/////////////////////////////////

void ToneTest::setup() {
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++) {
		pinMode(coilPins[i], OUTPUT);

		coilPinPorts[i] = portOutputRegister(digitalPinToPort(coilPins[i]));
		coilPinMaks[i] = digitalPinToBitMask(coilPins[i]);

		pd->port = portOutputRegister(digitalPinToPort(coilPins[i]));
		pd->mask = digitalPinToBitMask(coilPins[i]);
		pd++;
	}

    // 16 bit timer
    TCCR1A = 0;
    TCCR1B = 0;
    bitWrite(TCCR1B, WGM12, 1);
    bitWrite(TCCR1B, CS10, 1);

	speakerPort.port = portOutputRegister(digitalPinToPort(speakerPin));
	speakerPort.mask = digitalPinToBitMask(speakerPin);
	pinMode(speakerPin, OUTPUT);
	pinMode(ledPin, OUTPUT);
	digitalWrite(speakerPin, 0);
	digitalWrite(ledPin, 0);

	btn.initialize(buttonPin);
	rotor.initialize(rotorPinA, rotorPinB);
	rotor.setMinMax(50, 50000);
	rotor.setPosition(100);

	Serial.begin(9600);
}

static long curPosition = 0;
static long lastPosition = -1;
static boolean enabled = false;

void ToneTest::loop() {
	btn.update();
	rotor.update();

	curPosition = rotor.getPosition();
	if (btn.isPressed()) {
		enabled = !enabled;
		digitalWrite(ledPin, enabled);
	}

	if (enabled) {
		if (curPosition != lastPosition) {
			lastPosition = curPosition;
			playTimer1(curPosition);
			Serial.println(curPosition);
		}
	} else {
		stopTimer1();
		activeCoilState = 0;
		lastPosition = -1;
	}
}
