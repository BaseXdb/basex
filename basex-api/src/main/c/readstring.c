/* Copyright (c) 2005-12, Alexander Holupirek <alex@holupirek.de>, BSD license */
#include <err.h>
#include <errno.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <unistd.h>

#include "readstring.h"

static size_t READSTRING_MAX = 1024 * 1024 * 10; // 10MB

/**
 * Reads string from file descriptor into dynamically allocated string.
 *
 * A variable length of characters is read from fd until a \0 byte
 * is detected or a predefined maximum READSTRING_MAX is reached.
 * The read bytes are stored into a dynamically allocated buffer str.
 * It is the responsibility of the caller to free(3) str.
 *
 * @param fd file descriptor to read from
 * @param str address of the newly allocated c string
 * @return number of characters read or -1 in case of failure
 *         in case of an error str is set to NULL
 */
ssize_t
readstring(int fd, char **str)
{
	char b;
	int rb;            // # of read byte (-1, 0, or 1)
	size_t chars = 0;  // # of stored chars in str
	size_t size  = 32; // start capacity of alloc'ed string

	// allocate default capacity
	*str = calloc(size, sizeof(char));
	if (str == NULL) {
		warn("malloc failed.");
		return -1;
	}

	// read until \0 is detected or predefined maximum is reached
	while(1) {
		if (!(chars < size - 1)) { // reallocate
			if (size && 2 > SIZE_MAX / size) {
				errno = ENOMEM;
				warn("overflow");
				goto err;
			}
			size_t newsize = size * 2;
			if (newsize < READSTRING_MAX) {
				char *newstr;
				if ((newstr = realloc(*str, newsize)) == NULL) {
					warn("reallocation failed.");
					goto err;
				}
				*str  = newstr;
				size = newsize;
			} else {
				errno = ENOBUFS;
				warn("variable string exceeds maximum of %d"
					, READSTRING_MAX);
				goto err;
			}
		}
		rb = read(fd, &b, 1);
		if (rb == -1) {
			if (rb == EINTR) // Interrupted, try again
				continue;
			else {
				warn("Can not read");
				goto err;
			}
		}
		if (rb == 0) { // EOF
			warnx("Hmm, we expected a \\0 before EOF.");
			goto err;
		}
		// store another read char
		*((*str) + chars) = b;
		chars++;
		if (b == '\0') {  // We are done.
			break;
		}
	}
	
	return chars;

err:
	free(*str);
	*str = NULL;
	return -1;
}
