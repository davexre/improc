#include "SmoothValue.h"

void SmoothValue::initialize(int buffer[], const int bufferSize, const int defaultValue) {
	buf = buffer;
	bufSize = bufferSize;
	index = 0;
	reset(defaultValue);
}

void SmoothValue::reset(const int defaultValue) {
	min = max = defaultValue;
	sum = 0;
	for (int i = 0; i < bufSize; i++) {
		sum += buf[i] = defaultValue;
	}
}

void SmoothValue::addValue(const int value) {
	sum -= buf[index];
	sum += buf[index++] = value;
	if (value > max)
		max = value;
	if (value < min)
		min = value;
	if (index >= bufSize)
		index = 0;
}
