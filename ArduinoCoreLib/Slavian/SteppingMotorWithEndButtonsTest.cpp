#include "Arduino.h"
#include "utils.h"
#include "Button.h"
#include "AdvButton.h"
#include "SteppingMotor.h"
#include "StateLed.h"

DefineClass(SteppingMotorWithEndButtonsTest);

static const int buttonPin = 4;	// the number of the pushbutton pin
static const int ledPin = 5; // the number of the LED pin
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
	btn.initialize(new DigitalInputArduinoPin(buttonPin, true), false);
	btnStart.initialize(new DigitalInputArduinoPin(startButtonPin, true), false);
	btnEnd.initialize(new DigitalInputArduinoPin(endButtonPin, true), false);

	led.initialize(new DigitalOutputArduinoPin(ledPin, 0), states, size(states), true);
	motor.initialize(
			new DigitalOutputArduinoPin(stepperPin11),
			new DigitalOutputArduinoPin(stepperPin12),
			new DigitalOutputArduinoPin(stepperPin21),
			new DigitalOutputArduinoPin(stepperPin22));
	motorControl.initialize(&motor);
	//motorControl.motorCoilDelayBetweenStepsMicros = 100000;
	motor.motorCoilTurnOffMicros = 100000;

    Serial.begin(115200);
    Serial.println("Initialized");
    Serial.println("Press the button to start");
    paused = true;
    modeState = 0;
    minStep = maxStep = 0;
}

void SteppingMotorWithEndButtonsTest::loop() {
	btn.update();
	btnStart.update();
	btnEnd.update();

	led.update();
	motor.update();
	motorControl.update();

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
