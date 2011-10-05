#include "Arduino.h"
#include "utils.h"
#include "DigitalIO.h"
#include "AdvButton.h"
#include "RotaryEncoderAcceleration.h"
#include "SteppingMotor.h"
#include "StateLed.h"
#include "menu/Menu.h"

DefineClass(SteppingMotorTest2);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 6; // the number of the LED pin
static const int rotorPinA = 2;	// One quadrature pin
static const int rotorPinB = 3;	// the other quadrature pin

static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

static AdvButton btn;
static RotaryEncoderAcceleration rotor;
static StateLed led;
static DigitalOutputShiftRegister_74HC595 extenderOut;

static SteppingMotor_MosfetHBridge motor1;
static SteppingMotor_MosfetHBridge motor2;
static SteppingMotor_MosfetHBridge motor3;
static SteppingMotor_MosfetHBridge motor4;

static SteppingMotorControl motorControl1;
static SteppingMotorControl motorControl2;
static SteppingMotorControl motorControl3;
static SteppingMotorControl motorControl4;

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

void SteppingMotorTest2::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setMinMax(100, 5000);
	rotor.setValue(1000);
	attachInterrupt(0, UpdateRotor, CHANGE);
	extenderOut.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));

	motor1.initialize(
			extenderOut.createPinHandler(0),
			extenderOut.createPinHandler(1),
			extenderOut.createPinHandler(2),
			extenderOut.createPinHandler(3));
	motor2.initialize(
			extenderOut.createPinHandler(4),
			extenderOut.createPinHandler(5),
			extenderOut.createPinHandler(6),
			extenderOut.createPinHandler(7));
	motor3.initialize(
			extenderOut.createPinHandler(8),
			extenderOut.createPinHandler(9),
			extenderOut.createPinHandler(10),
			extenderOut.createPinHandler(11));
	motor4.initialize(
			extenderOut.createPinHandler(12),
			extenderOut.createPinHandler(13),
			extenderOut.createPinHandler(14),
			extenderOut.createPinHandler(15));

	motorControl1.initialize(&motor1);
	motorControl2.initialize(&motor2);
	motorControl3.initialize(&motor3);
	motorControl4.initialize(&motor4);

	motorControl1.resetStepTo(rotor.getValue());
	motorControl2.resetStepTo(rotor.getValue());
	motorControl3.resetStepTo(rotor.getValue());
	motorControl4.resetStepTo(rotor.getValue());

	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	motor1.motorCoilTurnOffMicros = 100000;
	motor2.motorCoilTurnOffMicros = 100000;
	motor3.motorCoilTurnOffMicros = 100000;
	motor4.motorCoilTurnOffMicros = 100000;

    Serial.begin(115200);
    Serial.println("Initialized");
}

void SteppingMotorTest2::loop() {
	btn.update();
	led.update();
	extenderOut.update();

	motor1.update();
	motor2.update();
	motor3.update();
	motor4.update();

	motorControl1.update();
	motorControl2.update();
	motorControl3.update();
	motorControl4.update();

	if (btn.isLongClicked()) {
		motorAutoRunning = !motorAutoRunning;
		if (motorAutoRunning) {
			motorControl1.rotate(motorForward);
			motorControl2.rotate(motorForward);
			motorControl3.rotate(motorForward);
			motorControl4.rotate(motorForward);
		} else {
			motorControl1.stop(); //resetStepTo(rotor.getValue());
			motorControl2.stop(); //resetStepTo(rotor.getValue());
			motorControl3.stop(); //resetStepTo(rotor.getValue());
			motorControl4.stop(); //resetStepTo(rotor.getValue());
		}
		Serial.print("RUNNING ");
		Serial.print((int)motorAutoRunning);
		Serial.print(" DELAY ");
		Serial.println(rotor.getValue());
	} else if (btn.isClicked()) {
		motorForward = !motorForward;
		if (motorAutoRunning) {
			motorControl1.rotate(motorForward);
			motorControl2.rotate(motorForward);
			motorControl3.rotate(motorForward);
			motorControl4.rotate(motorForward);
		}
		Serial.print("FORWARD ");
		Serial.print((int)motorForward);
		Serial.print(" DELAY ");
		Serial.println(rotor.getValue());
	}

	if (rotor.hasValueChanged()) {
		long val = rotor.getValue();
		//motor.motorCoilTurnOffMicros = rotor.getValue();
		motorControl1.gotoStep(val);
		motorControl2.gotoStep(val);
		motorControl3.gotoStep(val);
		motorControl4.gotoStep(val);
		Serial.println(val);
	}
}
