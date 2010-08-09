#include "SerialReader.h"

void SerialReader::initialize(int speed, int bufferSize, char *buffer) {
	Serial.begin(speed);
	setBuffer(bufferSize, buffer);
}

void SerialReader::setBuffer(int bufferSize, char *buffer) {
	this->bufferFull = 0;
	this->bufferSize = bufferSize;
	this->buffer = buffer;
	this->buffer[0] = 0;
	eol = false;
}

void SerialReader::update(void) {
	if (eol)
		return;
	while ((bufferSize > bufferFull + 1) && (Serial.available() > 0)) {
		if ((buffer[bufferFull] = Serial.read()) == '\n') {
			eol = true;
			buffer[bufferFull] = 0;
			break;
		}
		buffer[++bufferFull] = 0;
	}
	if (bufferSize <= bufferFull + 1) {
		eol = true;
	}
}

boolean SerialReader::available(void) {
	return eol;
}

char *SerialReader::readln() {
	if (eol) {
		eol = false;
		bufferFull = 0;
		return buffer;
	}
	return NULL;
}

SerialReader reader;
