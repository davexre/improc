#ifndef UTILS_H_
#define UTILS_H_

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

void delayLoop(unsigned long millis);

#endif
