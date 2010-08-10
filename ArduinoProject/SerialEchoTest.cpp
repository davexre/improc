#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "Button.h"
#include "SerialReader.h"

const int buttonPin = 4;	// the number of the pushbutton pin
const int ledPin = 13;		// the number of the LED pin
const int speakerPin = 8;

long frequency;		// if frequency==0 -> turn off
boolean isPlaying;
boolean wasButtonPressed;

BlinkingLed led;
Button btn;

char buf[200];

extern "C" void setup() {
	frequency = 0;
	isPlaying = false;
	wasButtonPressed = false;
	noTone(speakerPin);
	pinMode(speakerPin, OUTPUT);

	led.initialilze(ledPin);
	btn.initialize(buttonPin);
	reader.initialize(9600, size(buf), buf);
}

void showStatus() {
	Serial.print(frequency);
	Serial.print(":");
	Serial.print(isPlaying ? "1" : "0");
	Serial.print(":");
	Serial.print(wasButtonPressed ? "1" : "0");
	Serial.print(":");
	Serial.print(btn.isDown() ? "1" : "0");
	Serial.println();
}

void processReader() {
	char *c;
	int lightOn;
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	switch (c++[0]) {
	case 's':
		frequency = myatol(&c);
		wasButtonPressed = false;
		if (frequency == 0) {
			isPlaying = false;
			noTone(speakerPin);
		} else {
			isPlaying = true;
			tone(speakerPin, frequency);
		}
		led.playBlink(BLINK_FAST, isPlaying ? -1 : 0);
		break;
	case 'l':
		showStatus();
		break;
	}
}

extern "C" void loop() {
	led.update();
	btn.update();
	if (btn.isDown()) {
		wasButtonPressed = true;
		isPlaying = false;
		noTone(speakerPin);
		led.playBlink(BLINK_FAST, isPlaying ? -1 : 0);
		showStatus();
	}
	processReader();
}

#endif
