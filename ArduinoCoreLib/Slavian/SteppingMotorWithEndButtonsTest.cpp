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

static SteppingMotor_MosfetHBridge motor1;
static SteppingMotor_MosfetHBridge motor2;
static SteppingMotor_MosfetHBridge motor3;
static SteppingMotor_MosfetHBridge motor4;

static SteppingMotorControl motorControl1;
static SteppingMotorControl motorControl2;
static SteppingMotorControl motorControl3;
static SteppingMotorControl motorControl4;

static Button btnStartMotor1;
static Button btnStartMotor2;
static Button btnStartMotor3;
static Button btnStartMotor4;

static Button btnEndMotor1;
static Button btnEndMotor2;
static Button btnEndMotor3;
static Button btnEndMotor4;







static const int startButtonPin = 8;
static const int endButtonPin = 9;

static const int stepperPin11 = 12;	// BA6845FS Stepper motor driver OUT11 pin
static const int stepperPin12 = 13;	// BA6845FS Stepper motor driver OUT12 pin
static const int stepperPin21 = 10;	// BA6845FS Stepper motor driver OUT21 pin
static const int stepperPin22 = 11;	// BA6845FS Stepper motor driver OUT22 pin

static AdvButton btn;
static StateLed led;

static Button btnStart;
static Button btnEnd;
static SteppingMotor_MosfetHBridge motor;
static SteppingMotorControl motorControl;

static const unsigned int *states[] = {
		BLINK_SLOW,
		BLINK_MEDIUM,
		BLINK_OFF,
		BLINK_FAST,
		BLINK1, BLINK2, BLINK3
};

static bool paused;

static byte modeState;
static long minStep;
static long maxStep;

static void doTest() {
	switch (modeState) {
	case 0:
		motorControl.resetStepTo(0);
		if (btnStart.isDown()) {
			motorControl.stop();
		} else {
			motorControl.rotate(false);
		}
		modeState = 1;
		break;
	case 1:
		if (btnStart.isDown()) {
			modeState = 2;
			motorControl.resetStepTo(0);
			motorControl.rotate(true);
		}
		break;
	case 2:
		if (btnEnd.isDown()) {
			modeState = 3;
			maxStep = motorControl.getStep();
			motorControl.rotate(false);
		}
		break;
	case 3:
		if (btnStart.isDown()) {
			modeState = 100;
			minStep = motorControl.getStep();
			motorControl.stop();
		}
		break;
	default:
		break;
	}
}

void SteppingMotorWithEndButtonsTest::setup() {
	extenderOutput.initialize(16,
			new DigitalOutputArduinoPin(shiftRegisterOutputPinSH),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinST),
			new DigitalOutputArduinoPin(shiftRegisterOutputPinDS));
	extenderInput.initialize(DigitalInputShiftRegisterPinsCount,
			new DigitalOutputArduinoPin(shiftRegisterInputPinPE),
			new DigitalOutputArduinoPin(shiftRegisterInputPinCP),
			new DigitalInputArduinoPin(shiftRegisterInputPinQ7, false));

	motor1.initialize(
			extenderOutput.createPinHandler(0),
			extenderOutput.createPinHandler(1),
			extenderOutput.createPinHandler(2),
			extenderOutput.createPinHandler(3));
	motor2.initialize(
			extenderOutput.createPinHandler(4),
			extenderOutput.createPinHandler(5),
			extenderOutput.createPinHandler(6),
			extenderOutput.createPinHandler(7));
	motor3.initialize(
			extenderOutput.createPinHandler(8),
			extenderOutput.createPinHandler(9),
			extenderOutput.createPinHandler(10),
			extenderOutput.createPinHandler(11));
	motor4.initialize(
			extenderOutput.createPinHandler(12),
			extenderOutput.createPinHandler(13),
			extenderOutput.createPinHandler(14),
			extenderOutput.createPinHandler(15));

	motorControl1.initialize(&motor1);
	motorControl2.initialize(&motor2);
	motorControl3.initialize(&motor3);
	motorControl4.initialize(&motor4);

	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	//motor.motorCoilTurnOffMicros = 100000;

	btnStartMotor1.initialize(extenderInput.createPinHandler(0));
	btnEndMotor1.initialize(extenderInput.createPinHandler(1));

	btnStartMotor2.initialize(extenderInput.createPinHandler(2));
	btnEndMotor2.initialize(extenderInput.createPinHandler(3));

	btnStartMotor3.initialize(extenderInput.createPinHandler(4));
	btnEndMotor3.initialize(extenderInput.createPinHandler(5));

	btnStartMotor4.initialize(extenderInput.createPinHandler(6));
	btnEndMotor4.initialize(extenderInput.createPinHandler(7));

	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);

    Serial.begin(115200);
    Serial.println("Initialized");
    Serial.println("Press the button to start");
    paused = true;
    modeState = 0;
    minStep = maxStep = 0;
}

void SteppingMotorWithEndButtonsTest::loop() {
	extenderInput.update();
	extenderOutput.update();

	btnStartMotor1.update();
	btnStartMotor2.update();
	btnStartMotor3.update();
	btnStartMotor4.update();

	btnEndMotor1.update();
	btnEndMotor2.update();
	btnEndMotor3.update();
	btnEndMotor4.update();

	motor1.update();
	motor2.update();
	motor3.update();
	motor4.update();

	motorControl1.update();
	motorControl2.update();
	motorControl3.update();
	motorControl4.update();

	btn.update();
	led.update();





	if (btn.isClicked()) {
		paused = !paused;
		if (paused) {
			Serial.println("paused");
			motorControl.stop();
			modeState = 0;
		    minStep = maxStep = 0;
		} else {
			Serial.println("resumed");
		}
	}

	if (!paused) {
		doTest();
		if (modeState >= 100) {
			Serial.print("minStep: ");
			Serial.print(minStep);
			Serial.print(", maxStep: ");
			Serial.println(maxStep);
			modeState = 0;
		    minStep = maxStep = 0;
		}
	}
}
