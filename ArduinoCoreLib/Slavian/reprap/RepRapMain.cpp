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
static const int extruderFanPin = 5;
static const int extruderHeaterPin = 6;
static const int ledPin = 7;	// the number of the LED pin

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;
static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

////////////////////////////////////////////

static StateLed led;
static AdvButton btn;
static RotaryEncoderAcceleration rotor;
static bool lightOn = false;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_FAST
};

static char readerBuffer[200];
static SerialReader reader;

static DigitalOutputShiftRegister_74HC595 extenderOutput;
static DigitalInputShiftRegister_74HC166 extenderInput;

static TemperatureSensor *extruderTemperatureSensor;
static TemperatureSensor *bedTemperatureSensor;

static TemperatureControl extruderTemperatureControl;
static TemperatureControl bedTemperatureControl;

static SteppingMotor_MosfetHBridge motorX;
static SteppingMotor_MosfetHBridge motorY;
static SteppingMotor_MosfetHBridge motorZ;
static SteppingMotor_MosfetHBridge motorE;

static StepperAxis axisX;
static StepperAxis axisY;
static StepperAxis axisZ;
static StepperAxis axisE;

static RepRap reprap;

static void UpdateRotor() {
	rotor.update();
}

void RepRapMain::setup() {
	reader.initialize(115200, size(readerBuffer), readerBuffer);

	extenderOutput.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));
	extenderInput.initialize(9,
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

	extruderTemperatureSensor = new TemperatureSensor_TC1047(extruderTemperatureSensorPin);
	extruderTemperatureControl.initialize(extruderTemperatureSensor, extenderOutput.createPinHandler(extruderHeaterPin));

	bedTemperatureSensor = new TemperatureSensor_TC1047(bedTemperatureSensorPin);
	bedTemperatureControl.initialize(bedTemperatureSensor, extenderOutput.createPinHandler(bedHeaterPin));

	motorX.initialize(
			extenderOutput.createPinHandler(0),
			extenderOutput.createPinHandler(1),
			extenderOutput.createPinHandler(2),
			extenderOutput.createPinHandler(3));
	motorY.initialize(
			extenderOutput.createPinHandler(4),
			extenderOutput.createPinHandler(5),
			extenderOutput.createPinHandler(6),
			extenderOutput.createPinHandler(7));
	motorZ.initialize(
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));
	motorE.initialize(
			extenderOutput.createPinHandler(12),
			extenderOutput.createPinHandler(13),
			extenderOutput.createPinHandler(14),
			extenderOutput.createPinHandler(15));

	axisX.initialize(&motorX, extenderInput.createPinHandler(0), extenderInput.createPinHandler(1));
	axisY.initialize(&motorY, extenderInput.createPinHandler(2), extenderInput.createPinHandler(3));
	axisZ.initialize(&motorZ, extenderInput.createPinHandler(4), extenderInput.createPinHandler(5));
	axisE.initialize(&motorE, extenderInput.createPinHandler(6), extenderInput.createPinHandler(7));

	reprap.initialize(&reader, &extruderTemperatureControl, &bedTemperatureControl);
	reprap.axisX = &axisX;
	reprap.axisY = &axisY;
	reprap.axisZ = &axisZ;
	reprap.axisE = &axisE;
	reprap.fan = extenderOutput.createPinHandler(extruderFanPin);

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	attachInterrupt(0, UpdateRotor, CHANGE);

	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);
	led.setState(btn.isAutoRepeatEnabled());

	Serial.println("ok");
}

void RepRapMain::loop() {
	reader.update();
	extenderOutput.update();
	extruderTemperatureControl.update();
	bedTemperatureControl.update();
	led.update();
	btn.update();

	motorX.update();
	motorY.update();
	motorZ.update();
	motorE.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();
	reprap.update();
}
