#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "SteppingMotor.h"
#include "StateLed.h"

DefineClass(SteppingMotorWithEndButtonsTest);

static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin
static const int buttonPin = 4;
static const int ledPin = 6;

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;
static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

static DigitalOutputShiftRegister_74HC595 extenderOutput;
static DigitalInputShiftRegister_74HC166 extenderInput;

static SteppingMotor_MosfetHBridge motor[2];
static SteppingMotorControlWithButtons motorControl[2];

static AdvButton btn;
static StateLed led;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static bool paused;

void SteppingMotorWithEndButtonsTest::setup() {
	extenderOutput.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));
	extenderInput.initialize(9,
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

	motor[0].initialize(
			extenderOutput.createPinHandler(0),
			extenderOutput.createPinHandler(1),
			extenderOutput.createPinHandler(2),
			extenderOutput.createPinHandler(3));
	motor[1].initialize(
			extenderOutput.createPinHandler(4),
			extenderOutput.createPinHandler(5),
			extenderOutput.createPinHandler(6),
			extenderOutput.createPinHandler(7));
/*	motor[2].initialize(
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));*/
/*	motor[3].initialize(
			extenderOutput.createPinHandler(12),
			extenderOutput.createPinHandler(13),
			extenderOutput.createPinHandler(14),
			extenderOutput.createPinHandler(15));*/

//	motor[0].motorCoilTurnOffMicros = 300000;
//	motorControl[0].initialize(&motor[0], extenderInput.createPinHandler(1), extenderInput.createPinHandler(2));
//	motorControl[0].setDelayBetweenStepsMicros(2000);

	motorControl[0].initialize(&motor[0], extenderInput.createPinHandler(0), extenderInput.createPinHandler(8));
	motorControl[1].initialize(&motor[1], extenderInput.createPinHandler(1), extenderInput.createPinHandler(2));
//	motorControl[2].initialize(&motor[2], extenderInput.createPinHandler(4), extenderInput.createPinHandler(5));
//	motorControl[3].initialize(&motor[3], extenderInput.createPinHandler(6), extenderInput.createPinHandler(7));

	for (int i = 0; i < size(motorControl); i++) {
		motor[i].motorCoilTurnOffMicros = 300000;
		motorControl[i].setDelayBetweenStepsMicros(2000);
	}

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);

    Serial.begin(115200);
    Serial.println("Initialized");
    Serial.println("Press the button to start");
    paused = true;
}

unsigned long lastPrint;
bool prevBuffer[9];

void SteppingMotorWithEndButtonsTest::loop() {
	extenderInput.update();
	extenderOutput.update();
/*
	bool show = false;
	for (int i = 0; i < 9; i++) {
		bool val = extenderInput.getState(i);
		if (val != prevBuffer[i]) {
			show = true;
			prevBuffer[i] = val;
		}
	}

	if (show) {
		for (int i = 0; i < 9; i++) {
			bool val = extenderInput.getState(i);
			Serial.print(val ? '1' : '0');
			if (i % 4 == 3)
				Serial.print(' ');
		}
		Serial.println();
	}
*/
	for (int i = 0; i < size(motorControl); i++) {
		motor[i].update();
		motorControl[i].update();
	}
	btn.update();
	led.update();

	if (btn.isLongClicked()) {
		for (int i = 0; i < size(motorControl); i++) {
			Serial.print("motor[");
			Serial.print(i);
			Serial.println("]:");
			Serial.print("  isMoving:");
			Serial.println(motorControl[i].isMoving() ? "T": "F");

			Serial.print("  mode:");
			Serial.println((int)motorControl[i].mode);
			Serial.print("  modeState:");
			Serial.println((int)motorControl[i].modeState);
			Serial.print("  minStep:");
			Serial.println(motorControl[i].minStep);
			Serial.print("  maxStep:");
			Serial.println(motorControl[i].maxStep);
			Serial.print("  minStep:");
			Serial.println(motorControl[i].minStep);

			Serial.print("  mc.movementMode:");
			Serial.println((int)motorControl[i].motorControl.movementMode);
			Serial.print("  mc.step:");
			Serial.println(motorControl[i].motorControl.step);
			Serial.print("  mc.targetStep:");
			Serial.println(motorControl[i].motorControl.targetStep);
			Serial.print("  mc.stepsMadeSoFar:");
			Serial.println(motorControl[i].motorControl.stepsMadeSoFar);
			Serial.print("  mc.motorCoilOnMicros:");
			Serial.println(motorControl[i].motorControl.motorCoilOnMicros);
			Serial.print("  mc.delayBetweenStepsMicros:");
			Serial.println(motorControl[i].motorControl.delayBetweenStepsMicros);
			Serial.print("  now:");
			Serial.println(millis());
		}
	} else if (btn.isClicked()) {
		paused = !paused;
		if (paused) {
			Serial.println("paused");
			for (int i = 0; i < size(motorControl); i++)
				motorControl[i].stop();
		} else {
			Serial.println("resumed");
			for (int i = 0; i < size(motorControl); i++)
				motorControl[i].determineAvailableSteps();
			lastPrint = millis();
		}
	}

	if (!paused) {
		bool isMoving = false;
		for (int i = 0; i < size(motorControl); i++)
			if (motorControl[i].isMoving())
				isMoving = true;

		if (!isMoving) {
			bool isOk = true;
			for (int i = 0; i < size(motorControl); i++)
				if (!motorControl[i].isOk()) {
					Serial.print("ERROR in motor ");
					Serial.println(i);
					isOk = false;
					paused = true;
				}

			Serial.print("min/max ");
			for (int i = 0; i < size(motorControl); i++) {
				Serial.print(motorControl[i].getMinStep());
				Serial.print('\t');
				Serial.println(motorControl[i].getMaxStep());
				Serial.print('\t');
				if (isOk)
					motorControl[i].determineAvailableSteps();
			}
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
