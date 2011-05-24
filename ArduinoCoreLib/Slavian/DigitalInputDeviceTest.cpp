#include "Arduino.h"
#include "DigitalIO.h"

DefineClass(DigitalInputDeviceTest);

static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin

static const int shiftRegisterInputPinPE = 8;
static const int shiftRegisterInputPinCP = 9;
static const int shiftRegisterInputPinQ7 = 10;


class Button2 {
	DigitalInputPin *buttonPin;
	boolean lastState;
	boolean buttonState;
	long lastToggleTime;	// used to debounce the button

	/**
	 * Debounce time in milliseconds, default 10
	 */
	int debounce;

public:
	/**
	 * Initializes the class.
	 *
	 * pin		Number of the pin there the button is attached.
	 *
	 * debounceMillis
	 * 			The button debounce time in milliseconds.
	 */
	void initialize(DigitalInputPin *buttonPin, const int debounceMillis = 10);

	/**
	 * Updates the state of the rotary knob.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update(void);

	/**
	 * Has the button stated changed from isUp to isDown at the last update.
	 * This is to be used like an OnKeyDown.
	 */
	inline boolean isPressed(void) {
		return ((!buttonState) && lastState);
	}

	/**
	 * Has the button stated changed from isDown to isUp at the last update.
	 * This is to be used like an OnKeyUp.
	 */
	inline boolean isReleased(void) {
		return (buttonState && (!lastState));
	}

	/**
	 * Is the button down (pushed).
	 */
	inline boolean isDown(void) {
		return (!buttonState);
	}

	/**
	 * Is the button up.
	 */
	inline boolean isUp(void) {
		return (buttonState);
	}

	/**
	 * Has the state changed from up to down or vice versa.
	 */
	inline boolean isToggled(void) {
		return (buttonState != lastState);
	}
};

void Button2::initialize(DigitalInputPin *buttonPin, const int debounceMillis) {
	this->buttonPin = buttonPin;
	debounce = debounceMillis;
	lastToggleTime = millis();
	lastState = buttonState = buttonPin->getState();
}

void Button2::update() {
	boolean curReading = buttonPin->getState();
	long now = millis();
	lastState = buttonState;
	if (curReading != buttonState) {
		if (now - lastToggleTime >= debounce) {
			// Button state has not changed for #debounce# milliseconds. Consider it is stable.
			buttonState = curReading;
		}
		lastToggleTime = now;
	} else if (now - lastToggleTime >= debounce) {
		// Forward the last toggle time a bit
		lastToggleTime = now - debounce - 1;
	}
}















static Button2 btn;
static boolean lightOn = false;

DigitalInputShiftRegister digitalInputShiftRegister;
//DigitalInputPin *pin;


void DigitalInputDeviceTest::setup() {
	digitalInputShiftRegister.initialize(new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7));
//	pin = digitalInputShiftRegister.getPin(8);
//	pin = new DigitalInputArduinoPin(8, true);

	btn.initialize(new DigitalInputArduinoPin(buttonPin));

	pinMode(ledPin, OUTPUT);
//	btn.initialize(buttonPin);
	Serial.begin(115200);
}

void DigitalInputDeviceTest::loop() {
//	digitalInputShiftRegister.update();
//	lightOn = pin->getState();
	btn.update();
	if (btn.isPressed()) {
		lightOn = !lightOn;
		digitalWrite(ledPin, lightOn ? HIGH : LOW);
	}
}
