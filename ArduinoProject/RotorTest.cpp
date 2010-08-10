//#define UseThisFileForMainProgram
#ifdef UseThisFileForMainProgram

#include <WProgram.h>
#include "utils.h"
#include "Button.h"

extern "C" {

const int buttonPin = 4;     // the number of the pushbutton pin
const int ledPin =  13;      // the number of the LED pin
const int speakerPin = 8;

byte Blinker = 13;
int Delay = 250;
byte A = 2;        // One quadrature pin
byte B = 3;        // the other quadrature pin
volatile int Rotor = 0;
const long MaxRotor = 20;

Button btn;
boolean lightOn = false;
boolean speakerOn = false;

void UpdateRotation() {
    if (digitalRead(B)) {
        Rotor++;
    } else {
        Rotor--;
    }
    Rotor = constrain(Rotor, -MaxRotor, MaxRotor);
}

unsigned long toggle;
void setup() {
	pinMode(ledPin, OUTPUT);
	noTone(speakerPin);
	btn.initialize(buttonPin);

    // set DIO pins
    pinMode(Blinker, OUTPUT);
    pinMode(A, INPUT);
    pinMode(B, INPUT);
    // Turn on pullup resistors
    digitalWrite(A, HIGH);
    digitalWrite(B, HIGH);
    // Attach interrupt to pin A
    attachInterrupt(0, UpdateRotation, FALLING);
}

int counter = 0;
long pitch = 500;

extern "C" void loop() {
	btn.update();
	if (btn.isPressed()) {
		if (speakerOn) {
			noTone(speakerPin);
		}
		speakerOn = !speakerOn;
		toggle = millis();
	}
	unsigned long now = millis();
	if ((speakerOn) && (Rotor != 0)) {
		if (now - toggle > 0) {
			toggle = now + (MaxRotor - abs(Rotor)) * 10;
			pitch = constrain(pitch + Rotor, 1, 10000);

			tone(speakerPin, pitch);
			if (counter <= 0) {
				counter = Rotor;
				lightOn = !lightOn;
				digitalWrite(ledPin, lightOn ? HIGH : LOW);
			} else {
				counter--;
			}
		}
	} else {
		lightOn = false;
		digitalWrite(ledPin, LOW);
	}
	delayLoop(10);
}

}

#endif
