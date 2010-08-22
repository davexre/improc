//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "BlinkingLed.h"
#include "SerialReader.h"

// analog pins
const int presurePin = 0;		// the number of the pin of the presre sensor
const int temperaturePin1 = 1;	// the number of the pin of the temperature sensor of the cell
const int temperaturePin2 = 2;	// the number of the pin of the temperature sensor of the MOSFET

// digital pins
const int ledPin = 13;			// the number of the LED pin
const int speakerPin = 8;



boolean isPlaying;

int presureTreshold;
int maxPresure;
int curPresure;

int temperature1Threshold;
int curTemperature1;
int temperature2Threshold;
int curTemperature2;

BlinkingLed led;

char buf[20];

extern "C" void setup() {
	isPlaying = false;
	maxPresure = 0;
	curPresure = 0;
	presureTreshold = 140;
	temperature1Threshold = 123; //60 * 1024 / 500;	// 60 degrees
	temperature2Threshold = 164; //80 * 1024 / 500;	// 80 degrees
	noTone(speakerPin);
	pinMode(speakerPin, OUTPUT);

	led.initialize(ledPin);
	reader.initialize(9600, size(buf), buf);
}

void showStatus(boolean aborting) {
	Serial.print(curPresure);
	Serial.print("\t");
	Serial.print(isPlaying ? "1" : "0");
	Serial.print("\t");
	Serial.print(aborting ? "1" : "0");
	Serial.print("\t");
	Serial.print(maxPresure);
	Serial.print("\t");
	Serial.print(curTemperature1);
	Serial.print("\t");
	Serial.print(curTemperature2);
	Serial.println();
}

long frequency;
void processReader() {
	char *c;
	int lightOn;
	reader.update();
	if (!reader.available()) {
		return;
	}
	c = reader.readln();
	switch (c++[0]) {
	case 'a':	// Set temperature 1 threshold
		temperature1Threshold = strtol(c, &c, 10);
		break;
	case 'b':	// Set temperature 2 threshold
		temperature2Threshold = strtol(c, &c, 10);
		break;
	case 't':	// Set pressure threshold
		presureTreshold = strtol(c, &c, 10);
		break;
	case 's':	// Set new frequency. If frequency == 0 turn off
		frequency = strtol(c, &c, 10);
		maxPresure = curPresure;
		if ((frequency == 0) ||
			(curPresure > presureTreshold) ||
			(curTemperature1 > temperature1Threshold) ||
			(curTemperature2 > temperature2Threshold) ) {
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
	}
}

extern "C" void loop() {
	led.update();
	curPresure = analogRead(presurePin);
	curTemperature1 = analogRead(temperaturePin1);
	curTemperature2 = analogRead(temperaturePin2);

	processReader();
	if (maxPresure < curPresure)
		maxPresure = curPresure;
	if ((curPresure > presureTreshold) ||
		(curTemperature1 > temperature1Threshold) ||
		(curTemperature2 > temperature2Threshold) ) {
		// turn off
		noTone(speakerPin);
		led.playBlink(BLINK_FAST, 0);
		if (isPlaying) {
			isPlaying = false;
			showStatus(true);
			maxPresure = curPresure;
		}
	}
}

#endif
