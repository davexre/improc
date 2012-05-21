#include "Arduino.h"
#include "utils.h"
#include "TicksPerSecond.h"

DefineClass(LedSensorTest);

static const int ledPin = 13;
static const int ledSensorPinN = 8;
static const int ledSensorPinP = 9;

static bool isLedOn = false;
static unsigned long time;
static TicksPerSecond tps;
static float light;

void LedSensorTest::setup() {
	pinMode(ledPin, OUTPUT);
	digitalWrite(ledPin, LOW);

	pinMode(ledSensorPinN, OUTPUT);
	pinMode(ledSensorPinP, OUTPUT);

	tps.initialize(500);

	Serial.begin(115200);
	time = millis();
	light = 0;
}

unsigned int j;
void LedSensorTest::loop() {
	tps.tick();
	tps.update();

	pinMode(ledSensorPinN, OUTPUT);
	pinMode(ledSensorPinP, OUTPUT);
	digitalWrite(ledSensorPinN, HIGH);
	digitalWrite(ledSensorPinP, LOW);

	pinMode(ledSensorPinN, INPUT);
	digitalWrite(ledSensorPinN, LOW);

	for (j = 0; j < 30000; j++) {
		if (digitalRead(ledSensorPinN) == LOW)
			break;
	}

	isLedOn = !isLedOn;
	digitalWrite(ledPin, isLedOn ? HIGH : LOW);
	tps.smooth(j, &light, 500);
	if (time - millis() > 500) {
		Serial.print(j);
		Serial.print(' ');
		Serial.println(light);
		time = millis();
	}
}
