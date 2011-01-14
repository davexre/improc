#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
//#include <avr/pgmspace.h>
#include <pins_arduino.h>
//#include <stdint.h>

#include "utils.h"
//#include "Button.h"
//#include "RotorAcelleration.h"

volatile int32_t timer0_toggle_count;
volatile uint8_t *timer0_pin_port;
volatile uint8_t timer0_pin_mask;

/*
ISR(TIMER0_COMPA_vect)
{
  if (timer0_toggle_count != 0)
  {
    // toggle the pin
    *timer0_pin_port ^= timer0_pin_mask;

    if (timer0_toggle_count > 0)
      timer0_toggle_count--;
  }
  else
  {
    TIMSK0 &= ~(1 << OCIE0A);                 // disable the interrupt
    *timer0_pin_port &= ~(timer0_pin_mask);   // keep pin low after stop
  }
}
*/

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
} coilPorts[coilCount];

volatile uint8_t *coilPinPorts[coilCount];
volatile uint8_t coilPinMaks[coilCount];


void initialize() {
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++) {
		coilPinPorts[i] = portOutputRegister(digitalPinToPort(coilPins[i]));
		coilPinMaks[i] = digitalPinToBitMask(coilPins[i]);

		pd->port = portOutputRegister(digitalPinToPort(coilPins[i]));
		pd->mask = digitalPinToBitMask(coilPins[i]);
		pd++;
	}
}

void alabala() {
	// activeCoilState = (activeCoilState + 1) % coilStatesCount;
	// activeCoilState++;
	if (++activeCoilState >= coilStatesCount)
		activeCoilState = 0;
	const byte *states = coilStates[activeCoilState];
	pinDesc *pd = coilPorts;
	for (int i = 0; i < coilCount; i++, pd++) {
		//digitalWrite(coilPins[i], states[i]);
	    // set the pin
		if (*(states++)) {
			// set bit
			*pd->port |= pd->mask;
		} else {
			// clear bit
			*pd->port &= !pd->mask;
		}
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
