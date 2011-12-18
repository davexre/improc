#include "RepRap.h"

const char PROGMEM pgm_OK[] = "ok";

inline long pow2(long x) {
	return x*x;
}

void RepRap::initialize(SerialReader *reader,
		TemperatureControl *extruderTemperatureControl,
		TemperatureControl *bedTemperatureControl) {
	pcb.initialize();
	this->reader = reader;
	this->extruderTemperatureControl = extruderTemperatureControl;
	this->bedTemperatureControl = bedTemperatureControl;
	gCodeParser.initialize();
	feedRate = 0;
	speed = 0;
	setMode(RepRap::Idle);
}

void RepRap::doRepRap_Sleep200Millis() {
	switch(modeState) {
	case 0:
		timeStamp = millis();
		modeState = 1;
		break;
	case 1:
		if (timeStamp - millis() >= 200) {
			Serial.pgm_println(pgm_OK);
			setMode(RepRap::Idle);
		}
		break;
	}
}

void RepRap::doRepRap_InitializePositionForced() {
	switch(modeState) {
	case 0:
		pcb.axisX.initializePosition();
		pcb.axisY.initializePosition();
		modeState = 1;
		break;
	case 1:
		if (pcb.axisX.isIdle() && pcb.axisY.isIdle()) {
			setMode(RepRap::Idle);
		}
		break;
	}
}

void RepRap::doRepRap_InitializePosition() {
	switch(modeState) {
	case 0:
		if (axesSelected & CommandFlad_X) {
			pcb.axisX.initializePosition();
		}
		if (axesSelected & CommandFlad_Y) {
			pcb.axisY.initializePosition();
		}
		if (axesSelected & CommandFlad_Z) {
			pcb.axisZ.initializePosition();
		}
		if (axesSelected & CommandFlad_E) {
			// TODO: ???
		}
		modeState = 1;
		break;
	case 1:
		if ((axesSelected & CommandFlad_X) && (!pcb.axisX.isIdle()))
			break;
		if ((axesSelected & CommandFlad_Y) && (!pcb.axisY.isIdle()))
			break;
		if ((axesSelected & CommandFlad_Z) && (!pcb.axisZ.isIdle()))
			break;
		Serial.pgm_println(pgm_OK);
		setMode(RepRap::Idle);
		break;
	}
}

void RepRap::doRepRap_WaitForMotors() {
	switch(modeState) {
	case 0:
		if (pcb.axisX.isIdle() &&
			pcb.axisY.isIdle() &&
			pcb.axisZ.isIdle() &&
			pcb.axisE.isIdle()) {
			Serial.pgm_println(pgm_OK);
			setMode(RepRap::Idle);
		}
		break;
	}
}

void RepRap::doRepRap_ControlledMove() {
	switch(modeState) {
	case 0: {
		long maxd = abs(pcb.axisX.getAbsolutePositionMicroM() - 1000 * gCodeParser.X);
		maxd = max(abs(pcb.axisY.getAbsolutePositionMicroM() - 1000 * gCodeParser.Y), maxd);
		maxd = max(abs(pcb.axisZ.getAbsolutePositionMicroM() - 1000 * gCodeParser.Z), maxd);
		maxd = max(abs(pcb.axisE.getAbsolutePositionMicroM() - 1000 * gCodeParser.E), maxd);
		long timeMicros = 60000 * (maxd / feedRate); // TODO: Check me!!!
		pcb.axisX.moveToPositionMicroM(1000 * gCodeParser.X, timeMicros);
		pcb.axisY.moveToPositionMicroM(1000 * gCodeParser.Y, timeMicros);
		pcb.axisZ.moveToPositionMicroM(1000 * gCodeParser.Z, timeMicros);
		pcb.axisE.moveToPositionMicroM(1000 * gCodeParser.E, timeMicros);
		modeState = 1;
		break;
	}
	case 1: {
		if (pcb.axisX.isIdle() &&
			pcb.axisY.isIdle() &&
			pcb.axisZ.isIdle() &&
			pcb.axisE.isIdle()) {
			Serial.pgm_println(pgm_OK);
			setMode(RepRap::Idle);
		}
		break;
	}
	}
}

void RepRap::doRepRap_Stop() {
	switch(modeState) {
	case 0:
		pcb.axisX.stop();
		pcb.axisY.stop();
		pcb.axisZ.stop();
		pcb.axisE.stop();
		extruderTemperatureControl->stop();
		bedTemperatureControl->stop();
		pcb.fan->setState(true);
		modeState = 1;
		break;
	case 1:
		if (extruderTemperatureControl->getTemperature() < 60) {
			pcb.fan->setState(false);
			Serial.pgm_println(pgm_OK);
			setMode(RepRap::Idle);
		}
		break;
	}
}

void RepRap::doRepRap_WaitForTemperature() {
	if (extruderTemperatureControl->getTargetTemperature() ==
			extruderTemperatureControl->getTemperature()) {
		Serial.pgm_println(pgm_OK);
		setMode(RepRap::Idle);
	}
}

void RepRap::update() {
	switch(mode) {
	case Sleep200Millis:
		doRepRap_Sleep200Millis();
		break;

	case InitializePosition:
		doRepRap_InitializePosition();
		break;

	case InitializePositionForced:
		doRepRap_InitializePositionForced();
		break;

	case WaitForMotors:
		doRepRap_WaitForMotors();
		break;

	case ControlledMove:
		doRepRap_ControlledMove();
		break;

	case Stop:
		doRepRap_Stop();
		break;

	case WaitForTemperature:
		doRepRap_WaitForTemperature();
		break;

	case Idle:
	default:
		if (reader->available()) {
			if (gCodeParser.parse(reader->readln())) {
				executeGCode(&gCodeParser);
			} else {
				// TODO: Error parsing
			}
		}
		break;
	}
}

void RepRap::executeGCode(GCodeParser *gCode) {
	if (gCode->commandOccuraceFlag & CommandFlad_G) {
		if (!gCode->commandOccuraceFlag & CommandFlad_X) {
			gCode->X = pcb.axisX.getAbsolutePositionMicroM();
		}
		if (!gCode->commandOccuraceFlag & CommandFlad_Y) {
			gCode->Y = pcb.axisY.getAbsolutePositionMicroM();
		}
		if (!gCode->commandOccuraceFlag & CommandFlad_Z) {
			gCode->Z = pcb.axisZ.getAbsolutePositionMicroM();
		}
	}

	if (gCode->commandOccuraceFlag & CommandFlad_F) {
		feedRate = gCode->feedRate;
	}
	if (gCode->commandOccuraceFlag & CommandFlad_K) {
		speed = gCode->speed;
	}

	switch (gCode->gCode) {
	case GCodeParser::GCode_MoveRapid:
		pcb.axisX.moveToPositionMicroMFast(gCode->X);
		pcb.axisY.moveToPositionMicroMFast(gCode->Y);
		pcb.axisZ.moveToPositionMicroMFast(gCode->Z);
		setMode(RepRap::WaitForMotors);
		break;
	case GCodeParser::GCode_ControlledMove: {
		long length = pow2(pcb.axisX.getAbsolutePositionMicroM() - gCode->X);
		length += pow2(pcb.axisY.getAbsolutePositionMicroM() - gCode->Y);
		length += pow2(pcb.axisZ.getAbsolutePositionMicroM() - gCode->Z);
		length = (long) sqrt(length);

		long timeMicros = ((60000 * length) / speed) * 1000;
		pcb.axisX.moveToPositionMicroM(gCodeParser.X, timeMicros);
		pcb.axisY.moveToPositionMicroM(gCodeParser.Y, timeMicros);
		pcb.axisZ.moveToPositionMicroM(gCodeParser.Z, timeMicros);
		setMode(RepRap::ControlledMove);
		break;
	}
	case GCodeParser::GCode_GoHome: {
		axesSelected = gCode->commandOccuraceFlag & (CommandFlad_X | CommandFlad_Y | CommandFlad_Z);
		if (axesSelected == 0)
			axesSelected |= (CommandFlad_X | CommandFlad_Y | CommandFlad_Z);
		setMode(RepRap::InitializePosition);
		break;
	}
	case GCodeParser::GCode_Dwell: // Sleep for 200 milliseconds
		setMode(RepRap::Sleep200Millis);
		break;
	case GCodeParser::GCode_NoCommand:
	default:
		break;
	}

	switch (gCode->mCode) {
	case GCodeParser::MCode_CompulsoryStop:
	case GCodeParser::MCode_Shutdown:
	case GCodeParser::MCode_OptionalStop:
	case GCodeParser::MCode_ProgramEnd:
		setMode(RepRap::Stop);
		break;
	case GCodeParser::MCode_ExtruderSetTemperature:
		if (gCode->commandOccuraceFlag & CommandFlad_S)
			extruderTemperatureControl->setTargetTemperature(gCode->S);
		break;
	case GCodeParser::MCode_ExtruderGetTemperature:
		Serial.pgm_print(PSTR("ok T:"));
		Serial.println(extruderTemperatureControl->getTemperature());
		break;
	case GCodeParser::MCode_ExtruderFanOn:
		pcb.fan->setState(true);
		break;
	case GCodeParser::MCode_ExtruderFanOff:
		pcb.fan->setState(false);
		break;
	case GCodeParser::MCode_ExtruderSetTemperatureAndWait:
		if (gCode->commandOccuraceFlag & CommandFlad_S) {
			extruderTemperatureControl->setTargetTemperature(gCode->S);
		}
		setMode(RepRap::WaitForTemperature);
		break;
	case GCodeParser::MCode_SetLineNumber:
		break;
	case GCodeParser::MCode_SendDebugInfo:
		break;
	case GCodeParser::MCode_ExtruderSetPWM:
		break;
	case GCodeParser::MCode_GetPosition: {
		Serial.pgm_print(PSTR("ok C: X:"));
		float pos = (float) pcb.axisX.getAbsolutePositionMicroM() / 1000.0f;
		Serial.print(pos);

		Serial.pgm_print(PSTR(" Y:"));
		pos = (float) pcb.axisY.getAbsolutePositionMicroM() / 1000.0f;
		Serial.print(pos);

		Serial.pgm_print(PSTR(" Z:"));
		pos = (float) pcb.axisZ.getAbsolutePositionMicroM() / 1000.0f;
		Serial.print(pos);

		Serial.pgm_print(PSTR(" E:"));
		pos = (float) pcb.axisE.getAbsolutePositionMicroM() / 1000.0f;
		Serial.println(pos);
		break;
	}
	case GCodeParser::MCode_GetCapabilities:
		break;
	case GCodeParser::MCode_ExtruderWaitForTemperature:
		setMode(RepRap::WaitForTemperature);
		break;
	case GCodeParser::MCode_GetZeroPosition:
		break;
	case GCodeParser::MCode_ExtruderOpenValve:
// TODO:		axisE->rotate(true, FAST_E_FEEDRATE);
		break;
	case GCodeParser::MCode_ExtruderCloseValve:
		pcb.axisE.moveToPositionMicroMFast(pcb.axisE.getAbsolutePositionMicroM() - 1000); // move extruder 1 mm backwards
		break;
	case GCodeParser::MCode_ExtruderSetTemperature2:
		if (gCode->commandOccuraceFlag & CommandFlad_S) {
			bedTemperatureControl->setTargetTemperature(gCode->S);
		}
		break;
	case GCodeParser::MCode_NoCommand:
	default:
		break;
	}
}
