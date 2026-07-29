/* Copyright (c), Alexander Holupirek <alex@holupirek.de>, BSD license */
#include <err.h>
#include <openssl/evp.h>
#include <openssl/sha.h>
#include <string.h>

#include "sha256.h"

/**
 * Print ascii hex representation of sha256 value to newly allocated string.
 */
static int
sha256toa(unsigned char *md_value, unsigned int md_len, char **sha256_string)
{
	unsigned int i, j;
	int rc;
	int hex_len = 3; // hex length to be printed xx\0
	unsigned int length = 64; // length of sha256 ascii hex representation

	*sha256_string = calloc(length + 1, sizeof(char));
	if (*sha256_string == NULL) {
		warnx("Can not allocate memory for sha256 ascii hex string.");
		return -1;
	}

	for (i = 0, j = 0; i < md_len && j < length; i++, j += 2) {
		rc = snprintf((*sha256_string) + j, hex_len, "%02x", md_value[i]);
		if (!(rc > -1 && rc < hex_len)) {
			warnx("Construction of sha256 ascii hex string failed.");
			return -1;
		}
	}
	(*sha256_string)[length] = '\0'; // is already \0, but we are defensive

	return 0;
}

/**
 * Consult EVP_DigestInit(3SSL) for details.
 */
static char *
sha256_digest(int n, ...)
{
	EVP_MD_CTX* mdctx = EVP_MD_CTX_new();
	const EVP_MD *md;
	unsigned char md_value[EVP_MAX_MD_SIZE];
	unsigned int md_len = 0;
	int i, rc;
	char *string;
	char *sha256_result = NULL;

	OpenSSL_add_all_digests();

	md = EVP_sha256();
	if(!md)
		err(1, "Unknown message digest");

	EVP_MD_CTX_init(mdctx);
	EVP_DigestInit_ex(mdctx, md, NULL);
	va_list argPtr;
	va_start(argPtr, n);
	for (i = 0; i < n; i++) {
		string = va_arg(argPtr, char *);
		EVP_DigestUpdate(mdctx, string, strlen(string));
	}
	va_end(argPtr);
	EVP_DigestFinal_ex(mdctx, md_value, &md_len);
	EVP_MD_CTX_free(mdctx);
	EVP_cleanup();

	rc = sha256toa(md_value, md_len, &sha256_result);
	if (rc == -1)
		err(1, "sha256 digest failure.");

	return sha256_result;
}

/**
 * Compute 256bit SHA-256 digest for string.
 *
 * @param string from which digest shall be computed
 * @return Allocated C string containing the hex result representation is
 * returned. It should be passed to free(3). On failure NULL is returned.
 */
char *
sha256(const char *string)
{
	return sha256_digest(1, string);
}
