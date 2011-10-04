#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "SteppingMotor.h"
#include "StateLed.h"

DefineClass(SteppingMotorWithEndButtonsTest);

#define DigitalInputShiftRegisterPinsCount 16

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

static SteppingMotor_MosfetHBridge motor[3];
static SteppingMotorControlWithButtons motorControl[3];

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
	extenderInput.initialize(DigitalInputShiftRegisterPinsCount,
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
	motor[2].initialize(
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));
//	motor[3].initialize(
//			extenderOutput.createPinHandler(12),
//			extenderOutput.createPinHandler(13),
//			extenderOutput.createPinHandler(14),
//			extenderOutput.createPinHandler(15));

	motorControl[0].initialize(&motor[0], extenderInput.createPinHandler(0), extenderInput.createPinHandler(1));
	motorControl[1].initialize(&motor[1], extenderInput.createPinHandler(2), extenderInput.createPinHandler(3));
	motorControl[2].initialize(&motor[2], extenderInput.createPinHandler(4), extenderInput.createPinHandler(5));
//	motorControl[3].initialize(&motor[3], extenderInput.createPinHandler(6), extenderInput.createPinHandler(7));

	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	//motor.motorCoilTurnOffMicros = 100000;

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);

    Serial.begin(115200);
    Serial.println("Initialized");
    Serial.println("Press the button to start");
    paused = true;
}

void SteppingMotorWithEndButtonsTest::loop() {
	extenderInput.update();
	extenderOutput.update();

	for (int i = 0; i < size(motorControl); i++) {
		motor[i].update();
		motorControl[i].update();
	}
	btn.update();
	led.update();

	if (btn.isClicked()) {
		paused = !paused;
		if (paused) {
			Serial.println("paused");
			for (int i = 0; i < size(motorControl); i++)
				motorControl[i].stop();
		} else {
			Serial.println("resumed");
			for (int i = 0; i < size(motorControl); i++)
				motorControl[i].determineAvailableSteps();
		}
	}

	if (!paused) {
		bool isMoving = false;
		for (int i = 0; i < size(motorControl); i++)
			if (motorControl[i].isMoving())
				isMoving = true;

		if (!isMoving) {
			for (int i = 0; i < size(motorControl); i++) {
				Serial.print(motorControl[i].getMinStep());
				Serial.print('\t');
				Serial.println(motorControl[i].getMaxStep());
				Serial.print('\t');
				motorControl[i].determineAvailableSteps();
			}
		}
	}
}
