#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "SteppingMotor.h"
#include "StateLed.h"
#include "reprap/RepRapPCB.h"

DefineClass(SteppingMotorWithEndButtonsTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static AdvButton btn;
static StateLed led;

static RepRapPCB pcb;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static bool paused;

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;

void SteppingMotorWithEndButtonsTest::setup() {
	pcb.initialize();
	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);
	diLedPin.initialize(ledPin, 0);
	led.initialize(&diLedPin, states, size(states), true);

    Serial.begin(115200);
    Serial.println("Initialized");
    Serial.println("Press the button to start");
    paused = true;
}

unsigned long lastPrint;
bool prevBuffer[9];

static void dumpMotorControl(int i, SteppingMotorControlWithButtons &motorControl) {
	Serial.print("motor[");
	Serial.print(i);
	Serial.println("]:");
	Serial.print("  isMoving:");
	Serial.println(motorControl.isMoving() ? "T": "F");

/*			Serial.print("  mode:");
	Serial.println((int)motorControl.mode);
	Serial.print("  modeState:");
	Serial.println((int)motorControl.modeState);
	*/
	Serial.print("  minStep:");
	Serial.println(motorControl.getMinStep());
	Serial.print("  maxStep:");
	Serial.println(motorControl.getMaxStep());

	Serial.print("  mc.stepsMadeSoFar:");
	Serial.println(motorControl.getStepsMadeSoFar());
	Serial.print("  mc.delayBetweenStepsMicros:");
	Serial.println(motorControl.getDelayBetweenStepsMicros());
	Serial.print("  now:");
	Serial.println(millis());
}
static bool isOk = true;

static void checkError(int i, SteppingMotorControlWithButtons &motorControl) {
	if (!motorControl.isOk()) {
		Serial.print("ERROR in motor ");
		Serial.println(i);
		isOk = false;
		paused = true;
	}
}

static void printMinMax(int i, SteppingMotorControlWithButtons &motorControl) {
	Serial.print('\t');
	Serial.print(motorControl.getMinStep());
	Serial.print('\t');
	Serial.println(motorControl.getMaxStep());
	if (isOk)
		motorControl.determineAvailableSteps();
}

void SteppingMotorWithEndButtonsTest::loop() {
	pcb.update();

	bool show = false;
	for (int i = 0; i < 9; i++) {
		bool val = pcb.extenderInput.getState(i);
		if (val != prevBuffer[i]) {
			show = true;
			prevBuffer[i] = val;
		}
	}

	if (show) {
		for (int i = 0; i < 9; i++) {
			bool val = pcb.extenderInput.getState(i);
			Serial.print(val ? '1' : '0');
			if (i % 4 == 3)
				Serial.print(' ');
		}
		Serial.println();
	}

	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		dumpMotorControl(0, pcb.axisX.motorControl);
		dumpMotorControl(1, pcb.axisY.motorControl);
		dumpMotorControl(2, pcb.axisZ.motorControl);
	} else if (btn.isClicked()) {
		paused = !paused;
		if (paused) {
			Serial.println("paused");
			pcb.axisX.motorControl.stop();
			pcb.axisY.motorControl.stop();
			pcb.axisZ.motorControl.stop();
		} else {
			Serial.println("resumed");
			pcb.axisX.motorControl.determineAvailableSteps();
			pcb.axisY.motorControl.determineAvailableSteps();
			pcb.axisZ.motorControl.determineAvailableSteps();
			lastPrint = millis();
		}
	}

	if (!paused) {
		bool isMoving = false;
		if (pcb.axisX.motorControl.isMoving() ||
			pcb.axisY.motorControl.isMoving() ||
			pcb.axisZ.motorControl.isMoving())
			isMoving = true;

		if (!isMoving) {
			checkError(0, pcb.axisX.motorControl);
			checkError(1, pcb.axisY.motorControl);
			checkError(2, pcb.axisZ.motorControl);

			Serial.println("min/max:");
			printMinMax(0, pcb.axisX.motorControl);
			printMinMax(1, pcb.axisY.motorControl);
			printMinMax(2, pcb.axisZ.motorControl);
			lastPrint = millis();
		}
/*		else if (millis() - lastPrint > 500) {
			Serial.print("atStep ");
			for (int i = 0; i < size(motorControl); i++) {
				Serial.print(motorControl[i].getStep());
				Serial.print("\t");
			}
			Serial.println();
			lastPrint = millis();
		}*/
	}
}
