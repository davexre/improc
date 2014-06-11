#include "Arduino.h"
#include "utils.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"
#include "StateLed.h"

template<typename dummy=void>
class RotaryEncoderAccelerationTest {
	static const int buttonPin = 4;		// the number of the pushbutton pin
	static const int speakerPin = 8;
	static const int ledPin = 13;		// the number of the LED pin
	static const int rotorPinA = 2;		// One quadrature pin
	static const int rotorPinB = 3;		// the other quadrature pin

	static const unsigned int *const states[] PROGMEM;
	static RotaryEncoderAcceleration rotor;

	AdvButton btn;
	bool speakerOn = true;
	StateLed led;

	RotaryEncoderState ledState;
	RotaryEncoderState toneState;

	DigitalOutputArduinoPin diLedPin;
	DigitalInputArduinoPin diButtonPin;
	DigitalInputArduinoPin diRotorPinA;
	DigitalInputArduinoPin diRotorPinB;

	static void UpdateRotor() {
		rotor.update();
	}
public:
	void initialize() {
		pinMode(speakerPin, OUTPUT);
		diButtonPin.initialize(buttonPin, true);
		btn.initialize(&diButtonPin, false);
		diLedPin.initialize(ledPin, 0);
		led.initialize(&diLedPin, states, size(states), true);
		diRotorPinA.initialize(rotorPinA, true);
		diRotorPinB.initialize(rotorPinB, true);
		rotor.initialize(&diRotorPinA, &diRotorPinB);
		rotor.setState(&toneState);
		attachInterrupt(0, UpdateRotor, CHANGE);
		toneState.setValue(500);
		Serial.begin(115200);
		Serial.println("Push the encoder button to switch between changing pitch and blink");

		ledState.initialize(0, size(states) - 1, true);
		toneState.initialize(50, 15000, false);
	}

	void update() {
		btn.update();
		led.update();

		if (btn.isLongClicked()) {
			speakerOn = !speakerOn;
			if (speakerOn) {
				tone(speakerPin, toneState.getValue());
			} else {
				noTone(speakerPin);
			}
		} else if (btn.isClicked()) {
			rotor.setState(rotor.getState() == &toneState ? &ledState : &toneState);
		}

		if (toneState.hasValueChanged()) {
			long newTone = toneState.getValue();
			if (speakerOn) {
				tone(speakerPin, newTone);
			}
			float tps = rotor.tps.getTPS();
			Serial.print("Tone ");
			Serial.print(newTone);
			Serial.print(" ");
			Serial.println(tps);
		}

		if (ledState.hasValueChanged()) {
			int newLed = (int) ledState.getValue();
			led.setState(newLed);
			Serial.print("Led ");
			Serial.println(newLed);
		}
	}
};

template<typename dummy>
const unsigned int *const RotaryEncoderAccelerationTest<dummy>::states[] = {
	BLINK_SLOW,
	BLINK_MEDIUM,
	BLINK_OFF,
	BLINK_FAST,
	BLINK1, BLINK2, BLINK3
};

template<typename dummy>
RotaryEncoderAcceleration RotaryEncoderAccelerationTest<dummy>::rotor;
