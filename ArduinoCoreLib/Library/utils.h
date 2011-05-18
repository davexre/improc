#ifndef UTILS_H_
#define UTILS_H_

#include <avr/interrupt.h>

#define size(arr) sizeof(arr) / sizeof(arr[0])
#define disableInterrupts() uint8_t oldSREG = SREG; cli()
#define restoreInterrupts() SREG = oldSREG

/**
 * Converts string to int and forwards the pointer to the first non-digit symbol
 *
 * char *c;
 * long myLong;
 * if (c++[0] == 'L')
 *     myLong = myatol(&c);
 */
int myatoi(char **string);
long myatol(char **string);

void delayLoop(const unsigned long millis);

#endif
