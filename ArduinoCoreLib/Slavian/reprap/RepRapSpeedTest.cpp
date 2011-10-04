#include "Arduino.h"
#include "menu/Menu.h"
#include "utils.h"
#include "SoftwarePWM.h"
#include "TicksPerSecond.h"
#include "SerialReader.h"
#include "StepperAxis.h"
#include "TemperatureControl.h"

DefineClass(RepRapSpeedTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 6; // the number of the LED pin

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;
static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

// OUTPUT Expander Digital pins
static const int extruderHeaterPin = 0;
static const int bedHeaterPin = 1;
static const int extruderFanPin = 18;

///////

static AdvButton btn;
static bool speakerOn = true;

static MenuItem frequencyCyclesPerMinuteMenu;
static MenuItem pulseWidthMenu;

static MenuItem *menuItems[] = { &pulseWidthMenu, &frequencyCyclesPerMinuteMenu };

static SimpleMenuWithSerialPrint menu;

static SoftwarePWM spwm;

static DigitalOutputShiftRegister_74HC595 extenderOutput;
static DigitalInputShiftRegister_74HC166 extenderInput;
static TicksPerSecond tps;

static SteppingMotor_MosfetHBridge motorX;
static SteppingMotor_MosfetHBridge motorY;
static SteppingMotor_MosfetHBridge motorZ;
static SteppingMotor_MosfetHBridge motorE;

static StepperAxis axisX;
static StepperAxis axisY;
static StepperAxis axisZ;
static StepperAxis axisE;

static TemperatureControl temperatureControl;

static char serialReaderBuffer[200];
static SerialReader serialReader;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

void RepRapSpeedTest::setup() {
	extenderOutput.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));
	extenderInput.initialize(9,
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

	// Init menus
	frequencyCyclesPerMinuteMenu.initialize("Frequency", 0, 2000, false);
	pulseWidthMenu.initialize("Pulse Width", 0, 255, false);
	menu.initialize(new DigitalInputArduinoPin(rotorPinA, true), new DigitalInputArduinoPin(rotorPinB, true),
			new DigitalInputArduinoPin(buttonPin, true), menuItems, size(menuItems));
	attachInterrupt(0, updateRotaryEncoder, CHANGE);

	spwm.initialize(new DigitalOutputArduinoPin(ledPin), frequencyCyclesPerMinuteMenu.getValue());
	spwm.setValue(pulseWidthMenu.getValue());

	tps.initialize(500);

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

	temperatureControl.initialize(new TemperatureSensor_TC1047(0), extenderOutput.createPinHandler(16));

	serialReader.initialize(115200, sizeof(serialReaderBuffer), serialReaderBuffer);
    Serial.println("Push the encoder button to switch between menus");
}

bool allTurnedOn = false;

void RepRapSpeedTest::loop() {
	serialReader.update();
	extenderOutput.update();
	extenderInput.update();

	motorX.update();
	motorY.update();
	motorZ.update();
	motorE.update();

	axisX.update();
	axisY.update();
	axisZ.update();
	axisE.update();
	temperatureControl.update();

	tps.update(true);
	spwm.update();
	menu.update();

	if (serialReader.available())
		serialReader.readln();

	if (frequencyCyclesPerMinuteMenu.hasValueChanged()) {
		spwm.setFrequencyCyclesPerMinute(frequencyCyclesPerMinuteMenu.getValue());
	}
	if (pulseWidthMenu.hasValueChanged()) {
		spwm.setValue(pulseWidthMenu.getValue());
	}
	if (menu.button.isLongClicked()) {
		Serial.print("F=");
		Serial.print(spwm.getFrequencyCyclesPerMinute());
		Serial.print(" W=");
		Serial.print((int)spwm.getValue());
		Serial.print(" AllTrunedOn=");
		Serial.print(allTurnedOn ? "ON" : "OFF");
		Serial.print(" TPS=");
		Serial.println(tps.getTPS());

		allTurnedOn = !allTurnedOn;
		if (allTurnedOn) {
			axisX.initializeToStartingPosition();
			axisY.initializeToStartingPosition();
			axisZ.initializeToStartingPosition();
			axisE.initializeToStartingPosition();
			temperatureControl.setTargetTemperature(250);
		} else {
			axisX.stop();
			axisY.stop();
			axisZ.stop();
			axisE.stop();
			temperatureControl.stop();
		}
	}
}
