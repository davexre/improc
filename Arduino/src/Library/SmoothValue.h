#ifndef SMOOTHVALUE_H_
#define SMOOTHVALUE_H_

#include <Arduino.h>

/**
 * Smoothes a value by averaging.
 * Usage: In analogRead() to avoid value bouncing
 */
class SmoothValue {
private:
	int *buf;
	int bufSize;
	int index;
	int min;
	int max;
	long sum;
public:
	void initialize(int *buffer, const int bufferSize, const int defaultValue = 0) {
		buf = buffer;
		bufSize = bufferSize;
		index = 0;
		reset(defaultValue);
	}

	void addValue(const int value) {
		sum -= buf[index];
		sum += buf[index++] = value;
		if (value > max)
			max = value;
		if (value < min)
			min = value;
		if (index >= bufSize)
			index = 0;
	}

	void reset(const int defaultValue = 0) {
		min = max = defaultValue;
		sum = 0;
		for (int i = 0; i < bufSize; i++) {
			sum += buf[i] = defaultValue;
		}
	}

	inline int getAvg() {
		return sum / bufSize;
	}

	inline int getMax() {
		return max;
	}

	inline int getMin() {
		return min;
	}
};

#endif
