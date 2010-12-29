//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "Button.h"

const int ledPin =  13;      // the number of the LED pin
const int speakerPin = 8;

int asd = 0;



ISR(TIMER1_OVF_vect) {
	asd = 1234;
}

void init_NO() {
	TCCR1A = 0; // Waveform Generation Mode (WGM13:0)
	TCCR1B = 0; // Clock select (CS12:0)
//	OCIE11 = 1; //
}

extern "C" void setup() {
	pinMode(ledPin, OUTPUT);
	noTone(speakerPin);
//	btn.initialize(buttonPin);
//
//    // set DIO pins
//    pinMode(Blinker, OUTPUT);
//    pinMode(A, INPUT);
//    pinMode(B, INPUT);
//    // Turn on pullup resistors
//    digitalWrite(A, HIGH);
//    digitalWrite(B, HIGH);
//    // Attach interrupt to pin A
//    attachInterrupt(0, UpdateRotation, FALLING);
}

extern "C" void loop() {
	asd++;
}

#endif
