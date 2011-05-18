#ifndef SMOOTHVALUE_H_
#define SMOOTHVALUE_H_

#include <WProgram.h>

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
	void initialize(int *buffer, const int bufferSize, const int defaultValue = 0);

	void addValue(const int value);

	void reset(const int defaultValue = 0);

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
