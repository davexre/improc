#include "SerialReader.h"

void SerialReader::initialize(const int serialBoudRate,const  int bufferSize, char *buffer) {
	Serial.begin(serialBoudRate);
	setBuffer(bufferSize, buffer);
}

void SerialReader::setBuffer(const int bufferSize, char *buffer) {
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
		char c = Serial.read();
		if (c == '\n') {
			eol = true;
			buffer[bufferFull] = 0;
			break;
		}
		if (c != '\r') {	// Ignore carriage return
			buffer[bufferFull++] = c;
			buffer[bufferFull] = 0;
		}
	}
	if (bufferSize <= bufferFull + 1) {
		eol = true;
	}
}

char *SerialReader::readln() {
	if (eol) {
		eol = false;
		bufferFull = 0;
		return buffer;
	}
	return NULL;
}
