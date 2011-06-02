/* Copyright (c) 2011, Alexander Holupirek <alex@holupirek.de>, ISC license */

/**
 * Compute 128bit MD5 digest for string.
 *
 * @param string from which digest shall be computed
 * @return Allocated C string containing the hex result representation is
 * returned. It should be passed to free(3). On failure NULL is returned.
 */
char *md5(const char *string);
