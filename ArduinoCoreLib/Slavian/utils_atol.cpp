#include "utils.h"

long myatol(char **string) {
	char s;
	// skip trailing spaces
	while ((**string) && (**string == ' ')) {
		string[0]++;
	}

	// handle sign
	if (**string == '-') {
		s = 1;
		string[0]++;
	} else {
		s = 0;
	}

	long i = 0;
	char digit;
	while (**string) {
		digit = **string;
		if ((digit < '0') || (digit > '9'))
			break;
		i = (i << 3) + (i << 1) + (digit - '0');
		string[0]++;
	}
	return s ? -i : i;
}
