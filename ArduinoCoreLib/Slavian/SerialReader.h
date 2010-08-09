#ifndef SERIALREADER_H_
#define SERIALREADER_H_

#include "wiring.h"
#include "HardwareSerial.h"

class SerialReader {
private:
	int bufferSize;
	int bufferFull;
	char *buffer;
	boolean eol;
public:
	void initialize(int speed, int bufferSize, char *buffer);
	void setBuffer(int bufferSize, char *buffer);
	void update(void);
	boolean available();
	char *readln();
};

extern SerialReader reader;

#endif
