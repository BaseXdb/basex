/* Copyright (c), Alexander Holupirek <alex@holupirek.de>, BSD license */
#ifdef __cplusplus
extern "C" {
#endif
/**
 * Compute 256bit SHA-256 digest for string.
 *
 * @param string from which digest shall be computed
 * @return Allocated C string containing the hex result representation is
 * returned. It should be passed to free(3). On failure NULL is returned.
 */
char *sha256(const char *string);
#ifdef __cplusplus
}
#endif
