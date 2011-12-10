#include "Arduino.h"
#include "Button.h"
#include "Stepper.h"
#include "SteppingMotor.h"

DefineClass(MemoryTest);

DigitalInputArduinoPin startButton1;
DigitalInputArduinoPin endButton1;

DigitalOutputArduinoPin out11motor1;
DigitalOutputArduinoPin out12motor1;
DigitalOutputArduinoPin out21motor1;
DigitalOutputArduinoPin out22motor1;
StepperMotorMosfetHBridge motor1;
StepperMotorControlWithButtons mcontrol1;

SteppingMotor_MosfetHBridge motor2;
SteppingMotorControlWithButtons mcontrol2;


void MemoryTest::setup() {
	Serial.begin(115200);

	out11motor1.initialize(3, false);
	out12motor1.initialize(4, false);
	out21motor1.initialize(5, false);
	out22motor1.initialize(6, false);

	startButton1.initialize(1, true);
	endButton1.initialize(2, true);
	motor1.initialize(StepperMotor::HalfPower,
			&out11motor1,
			&out12motor1,
			&out21motor1,
			&out22motor1);
	mcontrol1.initialize(&motor1, &startButton1, &endButton1);

	motor2.initialize(SteppingMotor::HalfPower, SteppingMotor_MosfetHBridge::DoNotTurnOff,
			&out11motor1,
			&out12motor1,
			&out21motor1,
			&out22motor1);
	mcontrol2.initialize(&motor2, &startButton1, &endButton1);
}

void MemoryTest::loop() {
	mcontrol1.update();
	motor2.update();
	mcontrol2.update();
}
