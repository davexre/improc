#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
//#include <avr/pgmspace.h>
#include <pins_arduino.h>
//#include <stdint.h>

#include "utils.h"
//#include "Button.h"
//#include "RotorAcelleration.h"

const int ledPin =  13;      // the number of the LED pin
const int speakerPin = 8;

const int coilPins[] = { 5, 6, 7 };
const int coilCount = size(coilPins);

const byte coilStates[][coilCount] = {
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 1, 1},
		{0, 0, 1},
		{1, 0, 1}
};
const int coilStatesCount = size(coilStates);
int activeCoilState = 0;

struct pinDesc {
	volatile uint8_t *port;
	volatile uint8_t mask;
} coilPorts[coilCount], speakerPort;

volatile uint8_t *coilPinPorts[coilCount];
volatile uint8_t coilPinMaks[coilCount];


void initialize() {
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++) {
		pinMode(coilPins[i], OUTPUT);

		coilPinPorts[i] = portOutputRegister(digitalPinToPort(coilPins[i]));
		coilPinMaks[i] = digitalPinToBitMask(coilPins[i]);

		pd->port = portOutputRegister(digitalPinToPort(coilPins[i]));
		pd->mask = digitalPinToBitMask(coilPins[i]);
		pd++;
	}

	pinMode(speakerPin, OUTPUT);
	speakerPort.port = portOutputRegister(digitalPinToPort(speakerPin));
	speakerPort.mask = digitalPinToBitMask(speakerPin);

    // 16 bit timer
    TCCR1A = 0;
    TCCR1B = 0;
    bitWrite(TCCR1B, WGM12, 1);
    bitWrite(TCCR1B, CS10, 1);
}

// frequency in Hertz
void play(uint16_t frequency) {
	uint8_t prescalarbits = 0b001;
	int32_t toggle_count = 0;
	uint32_t ocr = 0;

	// Set the pinMode as OUTPUT
	pinMode(_pin, OUTPUT);

	// two choices for the 16 bit timers: ck/1 or ck/64
	ocr = F_CPU / frequency / 2 - 1;

	prescalarbits = 0b001;
	if (ocr > 0xffff) {
		ocr = F_CPU / frequency / 2 / 64 - 1;
		prescalarbits = 0b011;
	}

	if (_timer == 1)
		TCCR1B = (TCCR1B & 0b11111000) | prescalarbits;

    // Set the OCR for the given timer,
    // set the toggle count,
    // then turn on the interrupts
    OCR1A = ocr;
    timer1_toggle_count = toggle_count;
    bitWrite(TIMSK1, OCIE1A, 1);
}


void stop() {
	TIMSK1 &= ~(1 << OCIE1A);
	digitalWrite(_pin, 0);
}

bool isPlaying(void) {
  return TIMSK1 & (1 << OCIE1A);
}

void alabala() {
	const byte *states = coilStates[activeCoilState++];
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++, pd++) {
	    // set the pin
		if (*(states++)) {
			// set bit
			*pd->port |= pd->mask;
		} else {
			// clear bit
			*pd->port &= !pd->mask;
		}
	}
	if (activeCoilState >= coilStatesCount) {
		activeCoilState = 0;
		speakerPort ^= speakerPort.mask;
	}
}

/////////////////////////////////

extern "C" void setup() {
	alabala();
//	tone(0, 0, 0);
}

extern "C" void loop() {
}

#endif
