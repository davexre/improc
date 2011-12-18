#ifndef RepRap_h
#define RepRap_h

#include "SerialReader.h"
#include "GCodeParser.h"
#include "TemperatureControl.h"
#include "RepRapPCB2.h"

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
	enum RepRapMode {
		Idle = 0,
		Sleep200Millis = 1,
		InitializePosition = 2,
		InitializePositionForced = 3,
//		MoveRapid = 4,
		ControlledMove = 5,
		Stop = 6,
		WaitForTemperature = 7,
		WaitForMotors = 10,
	};
	RepRapMode mode;
	byte modeState;

	GCodeParser gCodeParser;
	void executeGCode(GCodeParser *gCode);

	TemperatureControl *extruderTemperatureControl;
	TemperatureControl *bedTemperatureControl;

	byte axesSelected;
	unsigned long timeStamp;

	inline void setMode(const RepRapMode mode) {
		this->mode = mode;
		this->modeState = 0;
	};

	void doRepRap_Sleep200Millis();
	void doRepRap_InitializePosition();
	void doRepRap_InitializePositionForced();
	void doRepRap_WaitForMotors();
	void doRepRap_ControlledMove();
	void doRepRap_Stop();
	void doRepRap_WaitForTemperature();
public:
	SerialReader *reader;
	RepRapPCB2 pcb;

	long speed;		// value is specified in MM per Minute
	long feedRate;	// value is specified in MM per Minute

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
