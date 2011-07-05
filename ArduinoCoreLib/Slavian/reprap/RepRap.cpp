#include "RepRap.h"

void RepRap::initialize(SerialReader *reader,
		TemperatureControl *extruderTemperatureControl,
		TemperatureControl *bedTemperatureControl) {
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

void RepRap::doRepRap_InitializeToStartingPosition() {
	switch(modeState) {
	case 0:
		if (axesSelected & CommandFlad_X) {
			axisX->initializeToStartingPosition();
			originX = positionX = 0;
		}
		if (axesSelected & CommandFlad_Y) {
			axisY->initializeToStartingPosition();
			originY = positionY = 0;
		}
		if (axesSelected & CommandFlad_Z) {
			axisZ->initializeToStartingPosition();
			originZ = positionZ = 0;
		}
		if (axesSelected & CommandFlad_E) {
			originE = positionE = 0;
		}
		modeState = 1;
		break;
	case 1:
		if ((axesSelected & CommandFlad_X) && (axisX->isMoving()))
			break;
		if ((axesSelected & CommandFlad_Y) && (axisY->isMoving()))
			break;
		if ((axesSelected & CommandFlad_Z) && (axisZ->isMoving()))
			break;
		Serial.println("ok");
		setMode(RepRap_Idle);
		break;
	}
}

void RepRap::doRepRap_MoveRapid() {
	switch(modeState) {
	case 0:
		axisX->moveToPositionMMFast(gCodeParser.X);
		axisY->moveToPositionMMFast(gCodeParser.Y);
		axisZ->moveToPositionMMFast(gCodeParser.Z);
		axisE->moveToPositionMMFast(gCodeParser.E);
		modeState = 1;
		break;
	case 1:
		if (!(axisX->isMoving() || axisY->isMoving() || axisZ->isMoving() || axisE->isMoving())) {
			Serial.println("ok");
			setMode(RepRap_Idle);
		}
		break;
	}
}

void RepRap::doRepRap_ControlledMove() {
	switch(modeState) {
	case 0: {
		float maxd = abs(axisX->getAbsolutePositionMM() - gCodeParser.X);
		maxd = max(abs(axisY->getAbsolutePositionMM() - gCodeParser.Y), maxd);
		maxd = max(abs(axisZ->getAbsolutePositionMM() - gCodeParser.Z), maxd);
		maxd = max(abs(axisE->getAbsolutePositionMM() - gCodeParser.E), maxd);
		float timeMillis = 60000 * (maxd / feedRate);
		axisX->moveToPositionMM(gCodeParser.X, timeMillis);
		axisY->moveToPositionMM(gCodeParser.Y, timeMillis);
		axisZ->moveToPositionMM(gCodeParser.Z, timeMillis);
		axisE->moveToPositionMM(gCodeParser.E, timeMillis);
		modeState = 1;
		break;
	}
	case 1: {
		if (!(axisX->isMoving() || axisY->isMoving() || axisZ->isMoving() || axisE->isMoving())) {
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
		axisX->stop();
		axisY->stop();
		axisZ->stop();
		axisE->stop();
		extruderTemperatureControl->stop();
		bedTemperatureControl->stop();
		fan->setState(true);
		modeState = 1;
		break;
	case 1:
		if (extruderTemperatureControl->getTemperature() < 60) {
			fan->setState(false);
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

	case RepRap_InitializeToStartingPosition:
		doRepRap_InitializeToStartingPosition();
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
		setMode(RepRap_InitializeToStartingPosition);
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
		fan->setState(true);
		break;
	case CodeM_ExtruderFanOff:
		fan->setState(false);
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
		axisE->rotate(true, FAST_E_FEEDRATE);
		break;
	case CodeM_ExtruderCloseValve:
		axisE->moveToPositionMMFast(axisE->getAbsolutePositionMM() - 1); // move extruder 1 mm backwards
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
