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
static const int ledPin = 13; // the number of the LED pin

static AdvButton btn;
static bool speakerOn = true;

static MenuItem frequencyCyclesPerMinuteMenu;
static MenuItem pulseWidthMenu;

static MenuItem *menuItems[] = { &pulseWidthMenu, &frequencyCyclesPerMinuteMenu };

static SimpleMenuWithSerialPrint menu;

static SoftwarePWM spwm;
static DigitalOutputShiftRegister_74HC164 outputShiftRegister;
static DigitalInputShiftRegister_74HC166 inputShiftRegister;
static TicksPerSecond tps;

static SteppingMotor_MosfetHBridge motorX;
static SteppingMotor_MosfetHBridge motorY;
static SteppingMotor_MosfetHBridge motorZ;
static SteppingMotor_MosfetHBridge motorE;

static StepperAxis stepperAxisX, stepperAxisY, stepperAxisZ, stepperAxisE;
static TemperatureControl temperatureControl;

static char serialReaderBuffer[200];
static SerialReader serialReader;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

void RepRapSpeedTest::setup() {
	inputShiftRegister.initialize(16, new DigitalOutputArduinoPin(9), new DigitalOutputArduinoPin(10), new DigitalInputArduinoPin(11, false));
	outputShiftRegister.initialize(16, new DigitalOutputArduinoPin(8), new DigitalOutputArduinoPin(9));
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
			outputShiftRegister.createPinHandler(0),
			outputShiftRegister.createPinHandler(1),
			outputShiftRegister.createPinHandler(2),
			outputShiftRegister.createPinHandler(3));
	motorY.initialize(
			outputShiftRegister.createPinHandler(4),
			outputShiftRegister.createPinHandler(5),
			outputShiftRegister.createPinHandler(6),
			outputShiftRegister.createPinHandler(7));
	motorZ.initialize(
			outputShiftRegister.createPinHandler(8),
			outputShiftRegister.createPinHandler(9),
			outputShiftRegister.createPinHandler(10),
			outputShiftRegister.createPinHandler(11));
	motorE.initialize(
			outputShiftRegister.createPinHandler(12),
			outputShiftRegister.createPinHandler(13),
			outputShiftRegister.createPinHandler(14),
			outputShiftRegister.createPinHandler(15));

	stepperAxisX.initialize(&motorX, inputShiftRegister.createPinHandler(0));
	stepperAxisY.initialize(&motorY, inputShiftRegister.createPinHandler(1));
	stepperAxisZ.initialize(&motorZ, inputShiftRegister.createPinHandler(2));
	stepperAxisE.initialize(&motorE, inputShiftRegister.createPinHandler(3));
	temperatureControl.initialize(new TemperatureSensor_TC1047(0), outputShiftRegister.createPinHandler(16));

	serialReader.initialize(115200, sizeof(serialReaderBuffer), serialReaderBuffer);
    Serial.println("Push the encoder button to switch between menus");
}

bool allTurnedOn = false;

void RepRapSpeedTest::loop() {
	serialReader.update();
	outputShiftRegister.update();
	inputShiftRegister.update();

	motorX.update();
	motorY.update();
	motorZ.update();
	motorE.update();

	stepperAxisX.update();
	stepperAxisY.update();
	stepperAxisZ.update();
	stepperAxisE.update();
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
			stepperAxisX.initializeToStartingPosition();
			stepperAxisY.initializeToStartingPosition();
			stepperAxisZ.initializeToStartingPosition();
			stepperAxisE.initializeToStartingPosition();
			temperatureControl.setTargetTemperature(250);
		} else {
			stepperAxisX.stop();
			stepperAxisY.stop();
			stepperAxisZ.stop();
			stepperAxisE.stop();
			temperatureControl.stop();
		}
	}
}
