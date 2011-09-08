#include "Arduino.h"
#include "utils.h"
#include "DigitalIO.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"
#include "SteppingMotor.h"
#include "StateLed.h"
#include "menu/Menu.h"

DefineClass(SteppingMotorTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 13; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static const int stepperPin11 = 8;	// BA6845FS Stepper motor driver OUT11 pin
static const int stepperPin12 = 9;	// BA6845FS Stepper motor driver OUT12 pin
static const int stepperPin21 = 10;	// BA6845FS Stepper motor driver OUT21 pin
static const int stepperPin22 = 11;	// BA6845FS Stepper motor driver OUT22 pin

static AdvButton btn;
static RotaryEncoderAcceleration rotor;
static StateLed led;
static SteppingMotor_MosfetHBridge motor;
static SteppingMotorControl motorControl;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static void UpdateRotor() {
	rotor.update();
}

static bool motorAutoRunning = false;
static bool motorForward = true;

void SteppingMotorTest::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setMinMax(100, 5000);
	rotor.setValue(1000);
	attachInterrupt(0, UpdateRotor, CHANGE);
	motor.initialize(
			new DigitalOutputArduinoPin(stepperPin11),
			new DigitalOutputArduinoPin(stepperPin12),
			new DigitalOutputArduinoPin(stepperPin21),
			new DigitalOutputArduinoPin(stepperPin22));
	motorControl.initialize(&motor);
	motorControl.resetStepTo(rotor.getValue());
	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	motor.motorCoilTurnOffMicros = 100000;

    Serial.begin(115200);
    Serial.println("Initialized");
}

void SteppingMotorTest::loop() {
	btn.update();
	led.update();
	motor.update();
	motorControl.update();

	if (btn.isLongClicked()) {
		motorAutoRunning = !motorAutoRunning;
		if (motorAutoRunning) {
			motorControl.rotate(motorForward);
		} else {
			motorControl.stop(); //resetStepTo(rotor.getValue());
		}
		Serial.print("RUNNING ");
		Serial.print((int)motorAutoRunning);
		Serial.print(" DELAY ");
		Serial.println(rotor.getValue());
	} else if (btn.isClicked()) {
		motorForward = !motorForward;
		if (motorAutoRunning) {
			motorControl.rotate(motorForward);
		}
		Serial.print("FORWARD ");
		Serial.print((int)motorForward);
		Serial.print(" DELAY ");
		Serial.println(rotor.getValue());
	}

	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		//motor.motorCoilTurnOffMicros = rotor.getValue();
		motorControl.gotoStep(val);
		Serial.println(val);
	}
}
