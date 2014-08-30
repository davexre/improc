/*
 * Test1.h
 *
 *  Created on: Jun 6, 2014
 *      Author: slavian
 */
#include <Arduino.h>

#ifndef TEST1_H_
#define TEST1_H_

namespace slavi {

class DigitalInputPin {
public:
	virtual bool getState() = 0;
};

class DigitalOutputPin { //: public DigitalInputPin {
public:
	virtual bool getState() = 0;

	virtual void setState(const bool value) = 0;
};

///////// DigitalInputArduinoPin

class DigitalInputArduinoPin : public DigitalInputPin {
private:
	uint8_t bit;

	volatile uint8_t *inputRegister;
public:
	void initialize(const uint8_t arduinoPin, const bool enablePullup) {
		bit = digitalPinToBitMask(arduinoPin);
		uint8_t port = digitalPinToPort(arduinoPin);
		inputRegister = portInputRegister(port);
		pinMode(arduinoPin, INPUT);

		volatile uint8_t *outputRegister = portOutputRegister(port);
		if (enablePullup) {
			disableInterrupts();
			*outputRegister |= bit;
			restoreInterrupts();
		} else {
			disableInterrupts();
			*outputRegister &= ~bit;
			restoreInterrupts();
		}
	}

	virtual bool getState() {
		return (*inputRegister & bit);
	}
};

///////// DigitalOutputArduinoPin

class DigitalOutputArduinoPin : public DigitalOutputPin {
private:
	uint8_t bit;

	volatile uint8_t *outputRegister;

	bool lastState;
public:
	void initialize(const uint8_t arduinoPin, const bool initialValue = 0) {
		bit = digitalPinToBitMask(arduinoPin);
		uint8_t port = digitalPinToPort(arduinoPin);
		outputRegister = portOutputRegister(port);
		pinMode(arduinoPin, OUTPUT);
		setState(initialValue);
	}

	virtual bool getState() {
		return lastState;
	}

	virtual void setState(const bool value) {
		lastState = value;
		if (value) {
			disableInterrupts();
			*outputRegister |= bit;
			restoreInterrupts();
		} else {
			disableInterrupts();
			*outputRegister &= ~bit;
			restoreInterrupts();
		}
	}
};


/**
 * A debounced button class.
 *
 * The debouncing is done via tracking the time of last
 * button toggle and not by delaying. This button class
 * uses the ATMEGA's internal pull-up resistors.
 *
 * The circuit: push button attached to pin_X from ground
 * http://www.arduino.cc/en/Tutorial/Debounce
 *
 * Optional components to hardware debounce a button:
 * http://www.ganssle.com/debouncing-pt2.htm
 *
 *     (internal 20k)       10k
 * pin:<---/\/\--------*----/\/\----|
 *                     |            |
 *              0.1uf ===            / switch
 *                     |            /
 * gnd:<---------------*------------|
 */
class Button {
	DigitalInputPin *buttonPin;
	bool lastState;
	bool currentState;
	unsigned long lastToggleTime;	// used to debounce the button

	/**
	 * Debounce time in milliseconds, default 10
	 */
	unsigned int debounce;

public:
	/**
	 * Initializes the class.
	 *
	 * pin		Number of the pin there the button is attached.
	 *
	 * debounceMillis
	 * 			The button debounce time in milliseconds.
	 */
	void initialize(DigitalInputPin *pin, const unsigned int debounceMillis = 10) {
		buttonPin = pin;
		debounce = debounceMillis;
		lastToggleTime = millis();
		lastState = currentState = buttonPin->getState();
	}

	/**
	 * Updates the state of the rotary knob.
	 * This method should be placed in the main loop of the program or
	 * might be invoked from an interrupt.
	 */
	void update() {
		bool curReading = buttonPin->getState();
		unsigned long now = millis();
		lastState = currentState;
		if (curReading != currentState) {
			if (now - lastToggleTime >= debounce) {
				// Button state has not changed for #debounce# milliseconds. Consider it is stable.
				currentState = curReading;
			}
			lastToggleTime = now;
		} else if (now - lastToggleTime >= debounce) {
			// Forward the last toggle time a bit
			lastToggleTime = now - debounce - 1;
		}
	}

	/**
	 * Has the button stated changed from isUp to isDown at the last update.
	 * This is to be used like an OnKeyDown.
	 */
	inline bool isPressed(void) {
		return ((!currentState) && lastState);
	}

	/**
	 * Has the button stated changed from isDown to isUp at the last update.
	 * This is to be used like an OnKeyUp.
	 */
	inline bool isReleased(void) {
		return (currentState && (!lastState));
	}

	/**
	 * Is the button down (pushed).
	 */
	inline bool isDown(void) {
		return (!currentState);
	}

	/**
	 * Is the button up.
	 */
	inline bool isUp(void) {
		return (currentState);
	}

	/**
	 * Has the state changed from up to down or vice versa.
	 */
	inline bool isToggled(void) {
		return (currentState != lastState);
	}

	void reset() {
		lastState = currentState;
		lastToggleTime = millis() - debounce - 1;
	}
};

/**
 * Blinks a led.
 * The led on/off is specified as time delays (in milliseconds)
 * Uses the timer0 via the millis() function.
 */
class BlinkingLed {
private:
	DigitalOutputPin *pin;

	const unsigned int *delays; // Delays must be stored in PROGMEM
	uint8_t curDelay;

	unsigned long toggleTime;
	signed short int playCount;
	bool lightOn;

	unsigned int getNextDelay() {
		unsigned int result;
		uint8_t oldSREG = SREG;
		cli();
		if (isPlaying()) {
			result = pgm_read_word(&(delays[curDelay++]));
			if (result == 0) {
				lightOn = false;
				curDelay = 0;
				if (playCount > 0)
					playCount--;
				if (playCount != 0) {
					result = pgm_read_word(&(delays[curDelay++]));
					if (result == 0) {
						curDelay = 0;
						playCount = 0;
					}
				}
			}
		} else {
			result = 0;
		}
		SREG = oldSREG;
		return result;
	}
public:
	/**
	 * Initializes the class.
	 *
	 * pin	The pin number the led is attached to.
	 */
	void initialize(DigitalOutputPin *pin) {
		this->pin = pin;
		this->delays = NULL;
		curDelay = 0;
		lightOn = false;
		playCount = 0;
		toggleTime = 0;
	}

	/**
	 * Updates the on/off state of the led. This method should be
	 * placed in the main loop of the program or might be invoked
	 * from an interrupt.
	 */
	void update() {
		if (isPlaying()) {
			unsigned long now = millis();
			if ((signed long) (now - toggleTime) >= 0) {
				unsigned int delta = getNextDelay();
				if (delta == 0) {
					lightOn = false;
				} else {
					lightOn = !lightOn;
				}
				toggleTime = now + delta;
				pin->setState(lightOn);
			}
		} else {
			lightOn = false;
			pin->setState(false);
		}
	}

	/**
	 * Starts playing the current blink sequence of led on/offs.
	 * This method is "thread safe"
	 *
	 * playCount
	 * 			The number of play backs to be performed on the
	 * 			current blink sequence. A negative value -1 means
	 * 			loop forever.
	 */
	void play(const signed short int playCount) {
		if (delays != NULL) {
			if (!isPlaying()) {
				toggleTime = millis();
			}
			this->playCount = playCount;
			update();
		}
	}

	/**
	 * Starts playing the current blink sequence of led on/offs.
	 * This method is "thread safe"
	 *
	 * delays
	 * 			Pointer to the blink sequence of led on/offs.
	 * 			A blink sequence must be terminated by a 0 value.
	 * playCount
	 * 			The number of play backs to be performed on the
	 * 			current blink sequence. A negative value -1 means
	 * 			loop forever.
	 */
	void playBlink(const unsigned int *delays, signed short int playCount) {
		if (delays == NULL)
			playCount = 0;
		if (delays == this->delays) {
			this->playCount = playCount;
		} else {
			disableInterrupts();
			curDelay = 0;
			this->delays = delays;
			this->playCount = playCount;
			lightOn = false;
			restoreInterrupts();
			toggleTime = millis();
		}
		update();
	}

	/**
	 * Starts playing the current blink sequence.
	 * This method is "thread safe"
	 */
	inline void start() {
		play(-1);
	}

	/**
	 * Stops playing the current blink sequence.
	 * This method is "thread safe"
	 */
	inline void stop() {
		play(0);
	}

	/**
	 * Returns TRUE if blink sequence is currently playing.
	 * This method is "thread safe"
	 */
	inline bool isPlaying() {
		return (playCount); // (playCount != 0)
	};

	/**
	 * Returns the state of the led.
	 * This method is "thread safe"
	 */
	inline bool isLedOn() {
		return (lightOn);
	}

	/**
	 * Returns the remaining number of loops for the current
	 * blink sequence. If the value is negative the blink
	 * sequence is looped.
	 * This method is "thread safe"
	 */
	inline signed short int getPlayCount() {
		return (playCount);
	}
};

// Blinking is defined as sequence of led on/off times (in milliseconds)
// LedOn, LedOff, LedOn, LedOff, 0
const unsigned int BLINK_DELAY_LONG = 500;
const unsigned int BLINK_DELAY_MEDIUM = 250;
const unsigned int BLINK_DELAY_SHORT = 50;

const unsigned int PROGMEM BLINK_SLOW[] = {
		BLINK_DELAY_LONG, BLINK_DELAY_LONG,
		0};
const unsigned int PROGMEM BLINK_MEDIUM[] = {
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK_FAST[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		0};
const unsigned int PROGMEM BLINK_OFF[] = {0};
const unsigned int PROGMEM BLINK_ON[] = {BLINK_DELAY_SHORT, 0};

const unsigned int PROGMEM BLINK1[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_SHORT,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK2[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_SHORT, BLINK_DELAY_MEDIUM,
		BLINK_DELAY_MEDIUM, BLINK_DELAY_MEDIUM,
		0};
const unsigned int PROGMEM BLINK3[] = {
		BLINK_DELAY_SHORT, BLINK_DELAY_LONG,
		0};

class BlinkingLedTest2 {
	static const int buttonPin = 4;	// the number of the pushbutton pin
	static const int ledPin = 13;		// the number of the LED pin

	Button btn;
	BlinkingLed led;

	DigitalOutputArduinoPin diLedPin;
	DigitalInputArduinoPin diButtonPin;
public:
	void setup(void) {
		diButtonPin.initialize(buttonPin, true);
		btn.initialize(&diButtonPin);
		diLedPin.initialize(ledPin, 0);
		led.initialize(&diLedPin);
	}

	void loop(void) {
		btn.update();
		led.update();
		if (btn.isPressed()) {
			led.playBlink(BLINK2, 1);
		}
	}
};

}

#endif /* TEST1_H_ */
