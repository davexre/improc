#include "StepperMotor.h"

const uint8_t StepperMotorMosfetHBridge::motorStatesMosfetHBridge_FullPower[] PROGMEM= {
		0b01010,
		0b00110,
		0b00101,
		0b01001
};

const uint8_t StepperMotorMosfetHBridge::motorStatesMosfetHBridge_HalfPower[] PROGMEM = {
		0b01000,
		0b00010,
		0b00100,
		0b00001,
};

const uint8_t StepperMotorMosfetHBridge::motorStatesMosfetHBridge_DoublePrecision[] PROGMEM = {
		0b01000,
		0b01010,
		0b00010,
		0b00110,
		0b00100,
		0b00101,
		0b00001,
		0b01001,
};

const char StepperMotorControlWithButtons::pgm_Up[] PROGMEM = "Up";
const char StepperMotorControlWithButtons::pgm_Down[] PROGMEM = "Down";

const uint8_t StepperMotorBA6845FS::motorStatesBA6845FS_HalfPower[] = {
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b00011  // H-bridge 1 - Stop,    H-bridge 2 - Reverse
};

const uint8_t StepperMotorBA6845FS::motorStatesBA6845FS_FullPower[] = {
		0b00111, // H-bridge 1 - Forward, H-bridge 2 - Reverse
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01111  // H-bridge 1 - Reverse, H-bridge 2 - Reverse
};

const uint8_t StepperMotorBA6845FS::motorStatesBA6845FS_DoublePrecision[] = {
		0b00100, // H-bridge 1 - Forward, H-bridge 2 - Stop
		0b00101, // H-bridge 1 - Forward, H-bridge 2 - Forward
		0b00001, // H-bridge 1 - Stop,    H-bridge 2 - Forward
		0b01101, // H-bridge 1 - Reverse, H-bridge 2 - Forward
		0b01100, // H-bridge 1 - Reverse, H-bridge 2 - Stop
		0b01111, // H-bridge 1 - Reverse, H-bridge 2 - Reverse
		0b00011, // H-bridge 1 - Stop,    H-bridge 2 - Reverse
		0b00111  // H-bridge 1 - Forward, H-bridge 2 - Reverse
};
