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

static const unsigned int PROGMEM *states[] = {
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

static DigitalOutputArduinoPin diLedPin;
static DigitalInputArduinoPin diButtonPin;
static DigitalInputArduinoPin diRotorPinA;
static DigitalInputArduinoPin diRotorPinB;
static DigitalOutputArduinoPin diStepMotor11pin;
static DigitalOutputArduinoPin diStepMotor12pin;
static DigitalOutputArduinoPin diStepMotor21pin;
static DigitalOutputArduinoPin diStepMotor22pin;

void SteppingMotorTest::setup() {
	diLedPin.initialize(ledPin);
	led.initialize(&diLedPin, states, size(states), true);

	diButtonPin.initialize(buttonPin, true);
	btn.initialize(&diButtonPin, false);

	diRotorPinA.initialize(rotorPinA, true);
	diRotorPinB.initialize(rotorPinB, true);
	rotor.initialize(&diRotorPinA, &diRotorPinB);
	rotor.setMinMax(100, 5000);
	rotor.setValue(1000);
	attachInterrupt(0, UpdateRotor, CHANGE);
	diStepMotor11pin.initialize(stepperPin11, 0);
	diStepMotor12pin.initialize(stepperPin12, 0);
	diStepMotor21pin.initialize(stepperPin21, 0);
	diStepMotor22pin.initialize(stepperPin22, 0);
	motor.initialize(
			SteppingMotor::FullPower,
			SteppingMotor_MosfetHBridge::TurnOffInSeparateCycle,
			&diStepMotor11pin, &diStepMotor12pin, &diStepMotor21pin, &diStepMotor22pin);
	motorControl.initialize(&motor);
	motorControl.resetStepTo(rotor.getValue());
	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	motor.setMotorCoilTurnOffMicros(100000);

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
