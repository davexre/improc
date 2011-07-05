#include "Arduino.h"
#include "DigitalIO.h"
#include "AdvButton.h"
#include "StateLed.h"
#include "SerialReader.h"
#include "TemperatureControl.h"
#include "StepperAxis.h"
#include "RepRap.h"

DefineClass(RepRapMain);

// Arduino Analog pins
static const int extruderTemperatureSensorPin = 0;
static const int bedTemperatureSensorPin = 1;

// Arduino Digital pins
static const int buttonPin = 4; // the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin
static const int shiftRegisterOutputPinCP = 8;
static const int shiftRegisterOutputPinDS = 9;
static const int axisX_EndPositionButtonPin = 10;
static const int axisY_EndPositionButtonPin = 11;
static const int axisZ_EndPositionButtonPin = 12;
static const int axisE_EndPositionButtonPin = 7;

// OUTPUT Expander Digital pins
static const int extruderHeaterPin = 0;
static const int bedHeaterPin = 1;
static const int extruderFanPin = 18;

static const int axisX_motor11Pin = 2;
static const int axisX_motor12Pin = 3;
static const int axisX_motor21Pin = 4;
static const int axisX_motor22Pin = 5;

static const int axisY_motor11Pin = 6;
static const int axisY_motor12Pin = 7;
static const int axisY_motor21Pin = 8;
static const int axisY_motor22Pin = 9;

static const int axisZ_motor11Pin = 10;
static const int axisZ_motor12Pin = 11;
static const int axisZ_motor21Pin = 12;
static const int axisZ_motor22Pin = 13;

static const int axisE_motor11Pin = 14;
static const int axisE_motor12Pin = 15;
static const int axisE_motor21Pin = 16;
static const int axisE_motor22Pin = 17;

////////////////////////////////////////////

static StateLed led;
static AdvButton btn;
static boolean lightOn = false;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_FAST
};

static char readerBuffer[200];
static SerialReader reader;

static DigitalOutputShiftRegister expanderOutput;

static TemperatureSensor *extruderTemperatureSensor;
static TemperatureSensor *bedTemperatureSensor;

static TemperatureControl extruderTemperatureControl;
static TemperatureControl bedTemperatureControl;

static StepperAxis axisX;
static StepperAxis axisY;
static StepperAxis axisZ;
static StepperAxis axisE;

static RepRap reprap;

void RepRapMain::setup() {
	reader.initialize(115200, size(readerBuffer), readerBuffer);

	expanderOutput.initialize(
			new DigitalOutputArduinoPin(shiftRegisterOutputPinCP),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));

	extruderTemperatureSensor = new TemperatureSensor_TC1047(extruderTemperatureSensorPin);
	extruderTemperatureControl.initialize(extruderTemperatureSensor, expanderOutput.createPinHandler(extruderHeaterPin));

	bedTemperatureSensor = new TemperatureSensor_TC1047(bedTemperatureSensorPin);
	bedTemperatureControl.initialize(bedTemperatureSensor, expanderOutput.createPinHandler(bedHeaterPin));

	axisX.initialize(new DigitalInputArduinoPin(axisX_EndPositionButtonPin, true),
			expanderOutput.createPinHandler(axisX_motor11Pin),
			expanderOutput.createPinHandler(axisX_motor12Pin),
			expanderOutput.createPinHandler(axisX_motor21Pin),
			expanderOutput.createPinHandler(axisX_motor22Pin));
	axisY.initialize(new DigitalInputArduinoPin(axisY_EndPositionButtonPin, true),
			expanderOutput.createPinHandler(axisY_motor11Pin),
			expanderOutput.createPinHandler(axisY_motor12Pin),
			expanderOutput.createPinHandler(axisY_motor21Pin),
			expanderOutput.createPinHandler(axisY_motor22Pin));
	axisZ.initialize(new DigitalInputArduinoPin(axisZ_EndPositionButtonPin, true),
			expanderOutput.createPinHandler(axisZ_motor11Pin),
			expanderOutput.createPinHandler(axisZ_motor12Pin),
			expanderOutput.createPinHandler(axisZ_motor21Pin),
			expanderOutput.createPinHandler(axisZ_motor22Pin));
	axisE.initialize(new DigitalInputArduinoPin(axisE_EndPositionButtonPin, true),
			expanderOutput.createPinHandler(axisE_motor11Pin),
			expanderOutput.createPinHandler(axisE_motor12Pin),
			expanderOutput.createPinHandler(axisE_motor21Pin),
			expanderOutput.createPinHandler(axisE_motor22Pin));

	reprap.initialize(&reader, &extruderTemperatureControl, &bedTemperatureControl);
	reprap.axisX = &axisX;
	reprap.axisY = &axisY;
	reprap.axisZ = &axisZ;
	reprap.axisE = &axisE;
	reprap.fan = expanderOutput.createPinHandler(extruderFanPin);

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin), states, size(states), true);
	led.setState(btn.isAutoRepeatEnabled());

	Serial.println("ok");
}

void RepRapMain::loop() {
	reader.update();
	expanderOutput.update();
	extruderTemperatureControl.update();
	bedTemperatureControl.update();
	led.update();
	btn.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();
	reprap.update();
}
