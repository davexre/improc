#include "RepRap.h"

void RepRap::initialize(SerialReader *reader,
		TemperatureControl *extruderTemperatureControl,
		TemperatureControl *bedTemperatureControl) {
	pcb.initialize();
	this->reader = reader;
	this->extruderTemperatureControl = extruderTemperatureControl;
	this->bedTemperatureControl = bedTemperatureControl;
	gCodeParser.initialize();
	isPositioningAbsolute = true;
	originX = originY = originZ = originE = 0;
	positionX = positionY = positionZ = positionE = 0;
	feedRate = 0;
	setMode(RepRap_Idle);
}

bool RepRap::isInitializePositionNeeded() {
	return
		pcb.axisX.isInitializePositionNeeded() ||
		pcb.axisY.isInitializePositionNeeded();
}

void RepRap::doRepRap_Sleep200Millis() {
	switch(modeState) {
	case 0:
		timeStamp = millis();
		modeState = 1;
		break;
	case 1:
		if (timeStamp - millis() >= 200) {
			Serial.println("ok");
			setMode(RepRap_Idle);
		}
		break;
	}
}

void RepRap::doRepRap_InitializePositionForced() {
	switch(modeState) {
	case 0:
		pcb.axisX.initializePosition();
		pcb.axisY.initializePosition();
		break;
	case 1:
		if ((!pcb.axisX.isMoving()) &&
			(!pcb.axisY.isMoving())) {
			setMode(RepRap_Idle);
		}
		break;
	}
}

void RepRap::doRepRap_InitializePosition() {
	switch(modeState) {
	case 0:
		if (axesSelected & CommandFlad_X) {
			pcb.axisX.initializePosition();
			originX = positionX = 0;
		}
		if (axesSelected & CommandFlad_Y) {
			pcb.axisY.initializePosition();
			originY = positionY = 0;
		}
		if (axesSelected & CommandFlad_Z) {
			pcb.axisZ.initializePosition();
			originZ = positionZ = 0;
		}
		if (axesSelected & CommandFlad_E) {
			originE = positionE = 0;
		}
		modeState = 1;
		break;
	case 1:
		if ((axesSelected & CommandFlad_X) && (pcb.axisX.isMoving()))
			break;
		if ((axesSelected & CommandFlad_Y) && (pcb.axisY.isMoving()))
			break;
		if ((axesSelected & CommandFlad_Z) && (pcb.axisZ.isMoving()))
			break;
		Serial.println("ok");
		setMode(RepRap_Idle);
		break;
	}
}

void RepRap::doRepRap_MoveRapid() {
	switch(modeState) {
	case 0:
		pcb.axisX.moveToPositionMicroMFast(1000 * gCodeParser.X);
		pcb.axisY.moveToPositionMicroMFast(1000 * gCodeParser.Y);
		pcb.axisZ.moveToPositionMicroMFast(1000 * gCodeParser.Z);
		pcb.axisE.moveToPositionMicroMFast(1000 * gCodeParser.E);
		modeState = 1;
		break;
	case 1:
		if (!(
				pcb.axisX.isMoving() ||
				pcb.axisY.isMoving() ||
				pcb.axisZ.isMoving() ||
				pcb.axisE.isMoving())) {
			Serial.println("ok");
			setMode(RepRap_Idle);
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
		if (!(
				pcb.axisX.isMoving() ||
				pcb.axisY.isMoving() ||
				pcb.axisZ.isMoving() ||
				pcb.axisE.isMoving())) {
			Serial.println("ok");
			setMode(RepRap_Idle);
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
			Serial.println("ok");
			setMode(RepRap_Idle);
		}
		break;
	}
}

void RepRap::doRepRap_WaitForTemperature() {
	if (extruderTemperatureControl->getTargetTemperature() ==
			extruderTemperatureControl->getTemperature()) {
		Serial.println("ok");
		setMode(RepRap_Idle);
	}
}

void RepRap::update() {
	switch(mode) {
	case RepRap_Sleep200Millis:
		doRepRap_Sleep200Millis();
		break;

	case RepRap_InitializePosition:
		doRepRap_InitializePosition();
		break;

	case RepRap_InitializePositionForced:
		doRepRap_InitializePositionForced();
		break;

	case RepRap_MoveRapid:
		doRepRap_MoveRapid();
		break;

	case RepRap_ControlledMove:
		doRepRap_ControlledMove();
		break;

	case RepRap_Stop:
		doRepRap_Stop();
		break;

	case RepRap_WaitForTemperature:
		doRepRap_WaitForTemperature();
		break;

	case RepRap_Idle:
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
		if (gCode->commandOccuraceFlag & CommandFlad_X) {
			if (isPositioningAbsolute)
				positionX = gCode->X;
			else
				positionX += gCode->X;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_Y) {
			if (isPositioningAbsolute)
				positionY = gCode->Y;
			else
				positionY += gCode->Y;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_Z) {
			if (isPositioningAbsolute)
				positionZ = gCode->Z;
			else
				positionZ += gCode->Z;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_E) {
			if (isPositioningAbsolute)
				positionE = gCode->E;
			else
				positionE += gCode->E;
		}
	}

	if (gCode->commandOccuraceFlag & CommandFlad_F) {
		feedRate = gCode->feedRate;
	}

	switch (gCode->gCode) {
	case CodeG_MoveRapid:
		setMode(RepRap_MoveRapid);
		break;
	case CodeG_ControlledMove:
		setMode(RepRap_ControlledMove);
		break;
	case CodeG_GoHome: {
		axesSelected = gCode->commandOccuraceFlag & (CommandFlad_X | CommandFlad_Y | CommandFlad_Z);
		if (axesSelected == 0)
			axesSelected |= (CommandFlad_X | CommandFlad_Y | CommandFlad_Z);
		setMode(RepRap_InitializePosition);
		break;
	}
	case CodeG_Dwell: // Sleep for 200 milliseconds
		setMode(RepRap_Sleep200Millis);
		break;
	case CodeG_UnitsInches:
		gCode->gCodeUnitsToMM = 25.4;
		break;
	case CodeG_UnitsMM:
		gCode->gCodeUnitsToMM = 1.0;
		break;
	case CodeG_PositioningAbsolute:
		isPositioningAbsolute = true;
		break;
	case CodeG_PositioningIncremental:
		isPositioningAbsolute = false;
		break;
	case CodeG_SetPosition: {
		if (gCode->commandOccuraceFlag & CommandFlad_X)	{
			originX = positionX - gCode->X;
			positionX = gCode->X;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_Y)	{
			originY = positionY - gCode->Y;
			positionY = gCode->Y;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_Z)	{
			originZ = positionZ - gCode->Z;
			positionZ = gCode->Z;
		}
		if (gCode->commandOccuraceFlag & CommandFlad_E)	{
			originE = positionE - gCode->E;
			positionE = gCode->E;
		}
		break;
	}
	case CodeG_NoCommand:
	default:
		break;
	}

	switch (gCode->mCode) {
	case CodeM_CompulsoryStop:
	case CodeM_Shutdown:
	case CodeM_OptionalStop:
	case CodeM_ProgramEnd:
		setMode(RepRap_Stop);
		break;
	case CodeM_ExtruderSetTemperature:
		if (gCode->commandOccuraceFlag & CommandFlad_S)
			extruderTemperatureControl->setTargetTemperature(gCode->S);
		break;
	case CodeM_ExtruderGetTemperature:
		Serial.print("ok T:");
		Serial.println(extruderTemperatureControl->getTemperature());
		break;
	case CodeM_ExtruderFanOn:
		pcb.fan->setState(true);
		break;
	case CodeM_ExtruderFanOff:
		pcb.fan->setState(false);
		break;
	case CodeM_ExtruderSetTemperatureAndWait:
		if (gCode->commandOccuraceFlag & CommandFlad_S) {
			extruderTemperatureControl->setTargetTemperature(gCode->S);
		}
		setMode(RepRap_WaitForTemperature);
		break;
	case CodeM_SetLineNumber:
		break;
	case CodeM_SendDebugInfo:
		break;
	case CodeM_ExtruderSetPWM:
		break;
	case CodeM_GetPosition:
		Serial.print("ok C: X:");
		Serial.print((positionX - originX) / gCode->gCodeUnitsToMM);
		Serial.print(" Y:");
		Serial.print((positionY - originY) / gCode->gCodeUnitsToMM);
		Serial.print(" Z:");
		Serial.print((positionZ - originZ) / gCode->gCodeUnitsToMM);
		Serial.print(" E:");
		Serial.println((positionE - originE) / gCode->gCodeUnitsToMM);
		break;
	case CodeM_GetCapabilities:
		break;
	case CodeM_ExtruderWaitForTemperature:
		setMode(RepRap_WaitForTemperature);
		break;
	case CodeM_GetZeroPosition:
		break;
	case CodeM_ExtruderOpenValve:
// TODO:		axisE->rotate(true, FAST_E_FEEDRATE);
		break;
	case CodeM_ExtruderCloseValve:
		pcb.axisE.moveToPositionMicroMFast(pcb.axisE.getAbsolutePositionMicroM() - 1000); // move extruder 1 mm backwards
		if (isInitializePositionNeeded())
			setMode(RepRap_InitializePositionForced);
		break;
	case CodeM_ExtruderSetTemperature2:
		if (gCode->commandOccuraceFlag & CommandFlad_S) {
			bedTemperatureControl->setTargetTemperature(gCode->S);
		}
		break;
	case CodeM_NoCommand:
	default:
		break;
	}
}
