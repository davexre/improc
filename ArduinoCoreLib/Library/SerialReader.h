#ifndef SERIALREADER_H_
#define SERIALREADER_H_

#include <Arduino.h>

/**
 * Utility class for "LINE" data processing.
 * Reads data from the Serial port and stores it in a zero terminated string.
 * The reading is terminated when the Line Feed LF(10) character is received
 * or when the buffer is full. Then the available() method will return TRUE.
 * After inoking readln() data reading from the Serial port is continued.
 */
class SerialReader {
private:
	int bufferSize;
	int bufferFull;
	char *buffer;
	bool eol;

public:
	/**
	 * Initializes the class.
	 *
	 * serialBoudRate
	 * 		Serial port boud rate. This parameter is passed down to Serial.begin()
	 *
	 * bufferSize
	 * 		Size of the buffer for reading serial data.
	 * 		If the LF (10) character is not received before the bufferSize is
	 * 		reached, i.e. buffer is full, then the available() method will
	 * 		return TRUE and no more Serial data will be read.
	 *
	 * buffer
	 * 		The user provided buffer for character data. The buffer is considered
	 * 		empty once the method readln() is invoked.
	 */
	void initialize(const long serialBoudRate, const int bufferSize, char *buffer);

	/**
	 * Reads any data available on the Serial port.
	 * This method should be placed in the main loop of the program.
	 */
	void update(void);

	/**
	 * Set a new buffer.
	 */
	void setBuffer(const int bufferSize, char *buffer);

	/**
	 * Returns a pointer to the user specified buffer if there is data availabe
	 * or NULL if no data is present and the buffer is empty. The character
	 * data in the buffer should be processed before the next invokation of
	 * the update() method. The character data is a zero terminated string.
	 */
	char *readln();

	/**
	 * Returns TRUE if the LF (Line Feed) character is reached.
	 * The CR (Carriage Return) character is ignored. The reading of
	 * data is discontinued until the readln() is invoked.
	 */
	inline bool available() {
		return (eol);
	};
};

#endif
