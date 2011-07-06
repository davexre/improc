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

// Feedrates in mm/minute
#define SLOW_XY_FEEDRATE 1000.0
#define FAST_XY_FEEDRATE 3000.0
#define SLOW_Z_FEEDRATE 20
#define FAST_Z_FEEDRATE  50.0
#define FAST_E_FEEDRATE  1000.0

#define SMALL_DISTANCE 0.01
#define SMALL_DISTANCE2 (SMALL_DISTANCE*SMALL_DISTANCE)

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

	bool isPositioningAbsolute;

	float originX, originY, originZ, originE;
	/**
	 * positionX = originX + gCode.X
	 */
	float positionX, positionY, positionZ, positionE;

	float feedRate;

	/**
	 * Initializes the class.
	 */
	void initialize(SerialReader *reader,
			TemperatureControl *extruderTemperatureControl,
			TemperatureControl *bedTemperatureControl);

	/**
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);
};

#endif
