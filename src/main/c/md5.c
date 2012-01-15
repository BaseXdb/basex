/* Copyright (c) 2005-12, Alexander Holupirek <alex@holupirek.de>, BSD license */
#include <err.h>
#include <openssl/evp.h>
#include <openssl/md5.h>
#include <string.h>

#include "md5.h"

/**
 * Print ascii hex representation of md5 value to newly allocated string.
 */
static int
md5toa(unsigned char *md_value, unsigned int md_len, char **md5_string)
{
	unsigned int i, j;
	int rc;
	int hex_len = 3; // hex length to be printed xx\0
	unsigned int length = 32; // length of md5 ascii hex representation

	*md5_string = calloc(length + 1, sizeof(char));
	if (*md5_string == NULL) {
		warnx("Can not allocate memory for md5 ascii hex string.");
		return -1;
	}

	for (i = 0, j = 0; i < md_len && j < length; i++, j += 2) {
		rc = snprintf((*md5_string) + j, hex_len, "%02x", md_value[i]);
		if (!(rc > -1 && rc < hex_len)) {
			warnx("Construction of md5 ascii hex string failed.");
			return -1;
		}
	}
	(*md5_string)[length] = '\0'; // is already \0, but we are defensive

	return 0;
}

/**
 * Consult EVP_DigestInit(3SSL) for details.
 */
static char *
md5_digest(int n, ...)
{
	EVP_MD_CTX mdctx;
	const EVP_MD *md;
	unsigned char md_value[EVP_MAX_MD_SIZE];
	unsigned int md_len = 0;
	int i, rc;
	char *string;
	char *md5_result = NULL;

	OpenSSL_add_all_digests();

	md = EVP_md5();
	if(!md)
		err(1, "Unknown message digest");

	EVP_MD_CTX_init(&mdctx);
	EVP_DigestInit_ex(&mdctx, md, NULL);
	va_list argPtr;
	va_start(argPtr, n);
	for (i = 0; i < n; i++) {
		string = va_arg(argPtr, char *);
		EVP_DigestUpdate(&mdctx, string, strlen(string));
	}
	va_end(argPtr);
	EVP_DigestFinal_ex(&mdctx, md_value, &md_len);
	EVP_MD_CTX_cleanup(&mdctx);
	EVP_cleanup();

	rc = md5toa(md_value, md_len, &md5_result);
	if (rc == -1)
		err(1, "md5 digest failure.");

	return md5_result;
}

/**
 * Compute 128bit MD5 digest for string.
 *
 * @param string from which digest shall be computed
 * @return Allocated C string containing the hex result representation is
 * returned. It should be passed to free(3). On failure NULL is returned.
 */
char *
md5(const char *string)
{
	return md5_digest(1, string);
}
