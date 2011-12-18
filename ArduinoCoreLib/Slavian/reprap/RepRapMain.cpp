#include "Arduino.h"
#include "DigitalIO.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "SerialReader.h"
#include "TemperatureControl.h"
#include "StepperAxis.h"
#include "RepRap.h"
#include "RotaryEncoderAcceleration.h"

DefineClass(RepRapMain);

// Arduino Analog pins
static const int extruderTemperatureSensorPin = 0;
static const int bedTemperatureSensorPin = 1;

// Arduino Digital pins
static const int bedHeaterPin = 1;
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin
static const int extruderHeaterPin = 6;
static const int ledPin = 7;	// the number of the LED pin

////////////////////////////////////////////

static StateLed led;
static AdvButton btn;
static RotaryEncoderAcceleration rotor;
static bool lightOn = false;

static const unsigned int PROGMEM *states[] = {
		BLINK_SLOW,
		BLINK_FAST
};

static char readerBuffer[100];
static SerialReader reader;

static DigitalOutputShiftRegister_74HC595 extenderOutput;
static DigitalInputShiftRegister_74HC166 extenderInput;

static TemperatureSensor *extruderTemperatureSensor;
static TemperatureSensor *bedTemperatureSensor;

static TemperatureControl extruderTemperatureControl;
static TemperatureControl bedTemperatureControl;

static RepRap reprap;

static void UpdateRotor() {
	rotor.update();
}

static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;
static DigitalOutputArduinoPin diLedPin;

void RepRapMain::setup() {
	reader.initialize(115200, size(readerBuffer), readerBuffer);

	extruderTemperatureSensor = new TemperatureSensor_TC1047(extruderTemperatureSensorPin);
	extruderTemperatureControl.initialize(extruderTemperatureSensor, extenderOutput.createPinHandler(extruderHeaterPin));

	bedTemperatureSensor = new TemperatureSensor_TC1047(bedTemperatureSensorPin);
	bedTemperatureControl.initialize(bedTemperatureSensor, extenderOutput.createPinHandler(bedHeaterPin));

	reprap.initialize(&reader, &extruderTemperatureControl, &bedTemperatureControl);

	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	attachInterrupt(0, UpdateRotor, CHANGE);

	diLedPin.initialize(ledPin);
	led.initialize(&diLedPin, states, size(states), true);
	led.setState(btn.isAutoRepeatEnabled());

	Serial.println("ok");
}

void RepRapMain::loop() {
	reader.update();
	extruderTemperatureControl.update();
	bedTemperatureControl.update();
	led.update();
	btn.update();

	reprap.update();
}
