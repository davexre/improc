#include "Arduino.h"
#include "menu/Menu.h"
#include "utils.h"
#include "SoftwarePWM.h"
#include "TicksPerSecond.h"
#include "SerialReader.h"
#include "TemperatureControl.h"
#include "RepRapPCB.h"

DefineClass(RepRapSpeedTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;	// the number of the pushbutton pin
static const int speakerPin = 8;
static const int ledPin = 6; // the number of the LED pin

// OUTPUT Expander Digital pins
static const int extruderHeaterPin = 0;
static const int bedHeaterPin = 1;
static const int extruderFanPin = 18;

///////

static bool speakerOn = true;

static MenuItem frequencyCyclesPerMinuteMenu;
static MenuItem pulseWidthMenu;
static MenuItem *menuItems[] = { &pulseWidthMenu, &frequencyCyclesPerMinuteMenu };
static SimpleMenuWithSerialPrint menu;

static SoftwarePWM spwm;
static TicksPerSecond tps;
static TemperatureControl temperatureControl;

static char serialReaderBuffer[200];
static SerialReader serialReader;

static RepRapPCB pcb;

static void updateRotaryEncoder() {
	menu.updateRotaryEncoder();
}

static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;
static DigitalOutputArduinoPin diLedPin;

void RepRapSpeedTest::setup() {
	pcb.initialize();
	// Init menus
	frequencyCyclesPerMinuteMenu.initialize("Frequency", 0, 2000, false);
	pulseWidthMenu.initialize("Pulse Width", 0, 255, false);
	diButtonPin.initialize(buttonPin, true);
	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);

	menu.initialize(&diRotorPinA, &diRotorPinB, &diButtonPin, menuItems, size(menuItems));
	attachInterrupt(0, updateRotaryEncoder, CHANGE);

	diLedPin.initialize(ledPin);
	spwm.initialize(&diLedPin, frequencyCyclesPerMinuteMenu.getValue());
	spwm.setValue(pulseWidthMenu.getValue());

	tps.initialize(500);
	temperatureControl.initialize(new TemperatureSensor_TC1047(0), pcb.extenderOutput.createPinHandler(16));

	serialReader.initialize(115200, sizeof(serialReaderBuffer), serialReaderBuffer);
    Serial.println("Push the encoder button to switch between menus");
}

bool allTurnedOn = false;

void RepRapSpeedTest::loop() {
	pcb.update();
	serialReader.update();
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
			pcb.axisX.initializePosition();
			pcb.axisY.initializePosition();
			pcb.axisZ.initializePosition();
			pcb.axisE.initializePosition();
			temperatureControl.setTargetTemperature(250);
		} else {
			pcb.axisX.stop();
			pcb.axisY.stop();
			pcb.axisZ.stop();
			pcb.axisE.stop();
			temperatureControl.stop();
		}
	}
}
