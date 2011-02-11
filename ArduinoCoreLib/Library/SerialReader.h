#ifndef SERIALREADER_H_
#define SERIALREADER_H_

#include "wiring.h"
#include "HardwareSerial.h"

class SerialReader {
//private:
public:
	int bufferSize;
	int bufferFull;
	char *buffer;
	boolean eol;

	void initialize(int speed, int bufferSize, char *buffer);
	void setBuffer(int bufferSize, char *buffer);
	void update(void);
	char *readln();
	inline boolean available() {
		return (eol);
	};
};

#endif
