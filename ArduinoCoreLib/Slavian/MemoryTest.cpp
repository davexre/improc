#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "reprap/RepRapPCB2.h"
#include "SerialReader.h"

DefineClass(MemoryTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static AdvButton btn;
static StateLed led;

static char readerBuffer[100];
static SerialReader reader;

static DigitalInputArduinoPin diButtonPin;
static DigitalOutputArduinoPin diLedPin;

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3,
		BLINK_ON, BLINK_OFF
};

const unsigned int *(*st);

static const char PROGMEM str[] = "This is a sample string 2 that is stored in PROGMEM";

static void print(const unsigned int *data) {
    int curDelay = 0;
    unsigned int res;
    while (res = pgm_read_word(&(data[curDelay++]))) {
    	Serial.println(res);
    }
    Serial.println();
}

void MemoryTest::setup() {
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

	reader.initialize(115200, size(readerBuffer), readerBuffer);

    Serial.println("Initialized123456");
    Serial.pgm_println(PSTR("This is a sample string that is stored in PROGMEM"));
    Serial.pgm_println(str);

    st = states;
    for (int i = 0; i < size(states); i++) {
    	print((unsigned int *)pgm_read_word(&(st[i])));
    }

//	Serial.println("abcde");
//    Serial.println("Press the button to stop");
}

void MemoryTest::loop() {
/*	reader.update();
	btn.update();
	led.update();

	if (reader.available()) {
	}
	if (btn.isLongClicked()) {
	} else if (btn.isClicked()) {
	}*/
}
