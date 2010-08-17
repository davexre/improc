//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "SerialReader.h"

const int presurePin = 0;	// the number of the pin of the presre sensor
const int ledPin = 13;		// the number of the LED pin
const int speakerPin = 8;

long frequency;		// if frequency==0 -> turn off
boolean isPlaying;
int maxPresure;
int curPresure;
int presureTreshold;

BlinkingLed led;

char buf[200];

extern "C" void setup() {
	frequency = 0;
	isPlaying = false;
	maxPresure = 0;
	curPresure = 0;
	presureTreshold = 140;
	noTone(speakerPin);
	pinMode(speakerPin, OUTPUT);

	led.initialilze(ledPin);
	reader.initialize(9600, size(buf), buf);
}

void showStatus(boolean aborting) {
	Serial.print(curPresure);
	Serial.print(":");
	Serial.print(isPlaying ? "1" : "0");
	Serial.print(":");
	Serial.print(aborting ? "1" : "0");
	Serial.print(":");
	Serial.print(frequency);
	Serial.print(":");
	Serial.print(maxPresure);
	Serial.print(":");
	Serial.print(presureTreshold);
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
//		frequency = myatol(&c);
		frequency = strtol(c, &c, 10);
		maxPresure = curPresure;
		if ((frequency == 0) || (curPresure > presureTreshold)) {
			isPlaying = false;
			noTone(speakerPin);
		} else {
			isPlaying = true;
			tone(speakerPin, frequency);
		}
		led.playBlink(BLINK_FAST, isPlaying ? -1 : 0);
		break;
	case 'l':
		showStatus(false);
		break;
	case 't':
//		presureTreshold = myatol(&c);
		presureTreshold = strtol(c, &c, 10);
		break;
	}
}

extern "C" void loop() {
	led.update();
	curPresure = analogRead(presurePin);
	processReader();
	if (maxPresure < curPresure)
		maxPresure = curPresure;
	if (curPresure > presureTreshold) {
		// turn off
		noTone(speakerPin);
		led.playBlink(BLINK_FAST, 0);
		if (isPlaying) {
			isPlaying = false;
			showStatus(true);
		}
	}
}

#endif
