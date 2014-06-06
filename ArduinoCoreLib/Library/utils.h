#ifndef UTILS_H_
#define UTILS_H_

#include <avr/interrupt.h>

#define size(arr) sizeof(arr) / sizeof(arr[0])
#define disableInterrupts() uint8_t oldSREG = SREG; cli()
#define restoreInterrupts() SREG = oldSREG

/**
 * Code borrowed from
 * http://provideyourown.com/2012/secret-arduino-voltmeter-measure-battery-voltage/

"In order to measure analog voltage accurately, we need an accurate voltage reference.
Most AVR chips provide three possible sources – an internal 1.1 volt source (some have
a 2.56 internal voltage source), an external reference source or Vcc. An external
voltage reference is the most accurate, but requires extra hardware. The internal
reference is stable, but has about a +/- 10% error. Vcc is completely untrustworthy in
most cases. The choice of the internal reference is inexpensive and stable, but most of
the time, we would like to measure a broader range, so the Vcc reference is the most
practical, but potentially the least accurate. In some cases it can be completely
unreliable!"

long millivolts = readVcc();
long measured = analogRead(A0);
long voltage = millivolts * measured / 1023; // answer is in millivolts
You don’t have to call readVcc everytime – just often enough to track the battery voltage.

 */
template <typename dummy>
long readVcc() {
	// Read 1.1V reference against AVcc
	// set the reference to Vcc and the measurement to the internal 1.1V reference
#if defined(__AVR_ATmega32U4__) || defined(__AVR_ATmega1280__) || defined(__AVR_ATmega2560__)
	ADMUX = _BV(REFS0) | _BV(MUX4) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
#elif defined (__AVR_ATtiny24__) || defined(__AVR_ATtiny44__) || defined(__AVR_ATtiny84__)
	ADMUX = _BV(MUX5) | _BV(MUX0);
#elif defined (__AVR_ATtiny25__) || defined(__AVR_ATtiny45__) || defined(__AVR_ATtiny85__)
	ADMUX = _BV(MUX3) | _BV(MUX2);
#else
	ADMUX = _BV(REFS0) | _BV(MUX3) | _BV(MUX2) | _BV(MUX1);
#endif

//	delay(2); // Wait for Vref to settle // TODO: Remove delay()
	ADCSRA |= _BV(ADSC); // Start conversion
	while (bit_is_set(ADCSRA, ADSC))
		; // measuring

	uint8_t low = ADCL; // must read ADCL first - it then locks ADCH
	uint8_t high = ADCH; // unlocks both

	long result = (high << 8) | low;

	result = 1125300L / result; // Calculate Vcc (in mV); 1125300 = 1.1*1023*1000
	return result; // Vcc in millivolts
}

/**
 * Converts string to int and forwards the pointer to the first non-digit symbol
 *
 * char *c;
 * long myLong;
 * if (c++[0] == 'L')
 *     myLong = myatol(&c);
 */
template <typename inttype> inttype myatoint(const char *string) {
	char s;
	// skip trailing spaces
	while ((*string) && (*string == ' ')) {
		string++;
	}

	// handle sign
	if (*string == '-') {
		s = 1;
		string++;
	} else {
		s = 0;
	}

	inttype i = 0;
	char digit;
	while (*string) {
		digit = *string;
		if ((digit < '0') || (digit > '9'))
			break;
		i = (i << 3) + (i << 1) + (digit - '0');
		string++;
	}
	return s ? -i : i;
}

const auto myatoi = myatoint<int>;
const auto myatol = myatoint<long>;

/**
 * unsigned long multiply subroutine - 31 cycles
 */
#define delayLoopExtraCalculations 52
#define delayLoopCPUCyclesPerIteration 10

template <typename dummy>
void delayLoop(const unsigned long millis) {
	unsigned long loop = ((F_CPU / 1000) / delayLoopCPUCyclesPerIteration)
			* millis - delayLoopExtraCalculations;
	while (loop > 0) {
		asm ("NOP;");
		loop--;
	}
}

#define DUMMY(type) \
inline void MIN(type &variable, type value) { \
	if (variable > value) \
		variable = value; \
}
DUMMY(unsigned short int)
DUMMY(signed short int)
DUMMY(signed int)
DUMMY(unsigned int)
DUMMY(signed long)
DUMMY(unsigned long)
DUMMY(float)
#undef DUMMY

#define DUMMY(type) \
inline void MAX(type &variable, type value) { \
	if (variable < value) \
		variable = value; \
}
DUMMY(unsigned short int)
DUMMY(signed short int)
DUMMY(signed int)
DUMMY(unsigned int)
DUMMY(signed long)
DUMMY(unsigned long)
DUMMY(float)
#undef DUMMY

#define DefineClass(className) \
class className { \
public: \
	static void setup(); \
	static void loop(); \
}

#endif
