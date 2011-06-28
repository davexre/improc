#ifndef RepRap_h
#define RepRap_h

#include "SerialReader.h"
#include "GCodeParser.h"
#include "TemperatureControl.h"
#include "StepperAxis.h"

#define RepRap_Idle 0
#define RepRap_Sleep200Millis 1
#define RepRap_InitializeToStartingPosition 2
#define RepRap_MoveRapid 3
#define RepRap_ControlledMove 4
#define RepRap_Stop 5
#define RepRap_WaitForTemperature 6

//////////
#define SLOW_XY_FEEDRATE 1000.0
#define FAST_XY_FEEDRATE 3000.0

class RepRap {
private:
	GCodeParser gCodeParser;
	void executeGCode(GCodeParser *gCode);

	TemperatureControl *extruderTemperatureControl;
	TemperatureControl *bedTemperatureControl;

	byte mode;
	byte modeState;
	byte axesSelected;
	unsigned long timeStamp;

	inline void setMode(const byte mode) {
		this->mode = mode;
		this->modeState = 0;
	};

	void doRepRap_Sleep200Millis();
	void doRepRap_InitializeToStartingPosition();
	void doRepRap_MoveRapid();
	void doRepRap_ControlledMove();
	void doRepRap_Stop();
	void doRepRap_WaitForTemperature();
public:
	SerialReader *reader;
	StepperAxis *axisX;
	StepperAxis *axisY;
	StepperAxis *axisZ;
	StepperAxis *axisE;
	DigitalOutputPin *fan;

	boolean isPositioningAbsolute;
	float gCodeUnitsToMM;

	float originX, originY, originZ, originE;
	float positionX, positionY, positionZ, positionE;

	/**
	 * Initializes the class.
	 */
	void initialize(SerialReader *reader);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);
};

#endif
