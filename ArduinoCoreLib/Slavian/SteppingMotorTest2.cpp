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

static const int shiftRegisterInputPinCP = 8;
static const int shiftRegisterInputPinPE = 9;
static const int shiftRegisterInputPinQ7 = 10;
static const int shiftRegisterOutputPinDS = 11;
static const int shiftRegisterOutputPinSH = 12;
static const int shiftRegisterOutputPinST = 13;

static AdvButton btn;
static RotaryEncoderAcceleration rotor;
static StateLed led;
static DigitalOutputShiftRegister_74HC595 extenderOut;
static DigitalInputShiftRegister_74HC166 extenderInput;

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
unsigned long lastPrint;

void SteppingMotorTest2::setup() {
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);
	rotor.initialize(
			new DigitalInputArduinoPin(rotorPinA, true),
			new DigitalInputArduinoPin(rotorPinB, true));
	rotor.setMinMax(100, 50000);
	rotor.setValue(2000);
	attachInterrupt(0, UpdateRotor, CHANGE);
	extenderOut.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));
	extenderInput.initialize(9,
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

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
	motor1.motorCoilTurnOffMicros = 300000;
	motor2.motorCoilTurnOffMicros = 300000;
	motor3.motorCoilTurnOffMicros = 300000;
	motor4.motorCoilTurnOffMicros = 300000;

    Serial.begin(115200);
    Serial.println("Initialized");
    lastPrint = millis();
}

bool prevBuffer[9];


void SteppingMotorTest2::loop() {
	btn.update();
	led.update();
	extenderOut.update();
	extenderInput.update();

	motor1.update();
	motor2.update();
	motor3.update();
	motor4.update();

	motorControl1.update();
	motorControl2.update();
	motorControl3.update();
	motorControl4.update();

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
/*		motorControl1.gotoStep(val);
		motorControl2.gotoStep(val);
		motorControl3.gotoStep(val);
		motorControl4.gotoStep(val);*/

		motorControl1.setDelayBetweenStepsMicros(val);
		motorControl2.setDelayBetweenStepsMicros(val);
		motorControl3.setDelayBetweenStepsMicros(val);
		motorControl4.setDelayBetweenStepsMicros(val);
		Serial.print("delay between steps=");
		Serial.println(val);
	}
	if (millis() - lastPrint > 500) {
		motor1.tps.update(false);
		Serial.print("M1.tps=");
		Serial.println(motor1.tps.getTPS());
		lastPrint = millis();
	}
}
