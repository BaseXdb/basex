/**
 * basexdbc.c : communicate with BaseX database server
 * Works with BaseX 7.x (but not with BaseX 8.0 and later)
 *
 * Copyright (c) 2005-12, Alexander Holupirek <alex@holupirek.de>, BSD license
 */
#include <assert.h>
#include <err.h>
#include <errno.h>
#include <netdb.h>
#include <openssl/evp.h>
#include <openssl/md5.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/poll.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include "basexdbc.h"
#include "md5.h"
#include "readstring.h"

static int send_db(int sfd, const char *buf, size_t buf_len);
static int basex_status(int sfd);

/**
 * Connect to host on port using stream sockets.
 *
 * @param host string representing host to connect to
 * @param port string representing port on host to connect to
 * @return socket file descriptor or -1 in case of failure
 */
int
basex_connect(const char *host, const char *port)
{
	struct addrinfo hints;
	struct addrinfo *result = NULL, *rp;
	int sfd, rc;

	if (host == NULL || port == NULL) {
#if DEBUG
		warnx("Missing hostname '%s' / port '%s'.", host, port);
#endif
		return -1;
	}

	/* Obtain address(es) matching host/port */
	memset(&hints, 0, sizeof(struct addrinfo));
	hints.ai_family   = AF_UNSPEC;       /* Allows IPv4 or IPv6 */
	hints.ai_socktype = SOCK_STREAM;     /* TCP socket */
	hints.ai_flags    = AI_NUMERICSERV;

	rc = getaddrinfo(host, port, &hints, &result);
	if (rc != 0) {
#if DEBUG
		warnx("getaddrinfo: %s", gai_strerror(rc));
#endif
		return -1;
	}

	/* getaddrinfo() returns a list of address structures.
	 * Try each address until we successfully connect(2).
	 * If socket(2) (or connect(2)) fails, we (close the
	 * socket and) try the next address. */
	for (rp = result; rp != NULL; rp = rp->ai_next) {
		sfd = socket(rp->ai_family, rp->ai_socktype, rp->ai_protocol);
		if (sfd == -1)
			continue; /* On error, try next address */

		if (connect(sfd, rp->ai_addr, rp->ai_addrlen) != -1)
			break;	/* Success */
		
		close(sfd); /* Connect failed: close socket, try next address */
	}

	if (rp == NULL) {	/* No address succeeded */
		warnx("Can not connect to BaseX server.");
		warnx("Hostname '%s', port %s.", host, port);
		return -1;
	}

	freeaddrinfo(result);	/* No longer needed */

	return sfd; /* This file descriptor is ready for I/O. */
}

/**
 * Authenticate against BaseX server connected on sfd using user and passwd.
 *
 * Authentication as defined by BaseX transfer protocol (BaseX 7.0 ff.):
 * https://github.com/BaseXdb/basex-api/blob/master/etc/readme.txt
 * {...} = string; \n = single byte
 *
 *   1. Client connects to server socket (basex_connect)
 *   2. Server sends timestamp: {timestamp} \0
 *   3. Client sends username and hash:
 *      {username} \0 {md5(md5(password) + timestamp)} \0
 *   4. Server sends \0 (success) or \1 (error)
 *
 * @param sfd socket file descriptor successfully connected to BaseX server
 * @param user string with database username
 * @param passwd string with password for database username
 * @return 0 in case of success, -1 in case of failure
 */
int
basex_authenticate(int sfd, const char *user, const char *passwd)
{
	char ts[BUFSIZ]; /* timestamp returned by basex. */
	char *md5_pwd;   /* md5'ed passwd */
	int ts_len, rc, i;

	/* Right after the first connect BaseX returns a nul-terminated
         * timestamp string. */
	memset(ts, 0, BUFSIZ);
	rc = read(sfd, &ts, BUFSIZ);
	if (rc == -1) {
		warnx("Reading timestamp failed.");
		return -1;
	}
	ts_len = strlen(ts);

#if DEBUG
	warnx("timestamp       : %s (%d)", ts, strlen(ts));
#endif

	/* BaseX Server expects an authentification sequence:
 	 * {username}\0{md5(md5(password) + timestamp)}\0 */

	/* Send {username}\0 */
	int user_len = strlen(user) + 1;
	rc = write(sfd, user, user_len);
	if (rc == -1 || rc != user_len) {
		warnx("Sending username failed. %d != %d", rc, user_len);
		return -1;
	}

	/* Compute md5 for passwd. */
	md5_pwd = md5(passwd);
	if (md5_pwd == NULL) {
		warnx("md5 computation for password failed.");
		return -1;
	}
	int md5_pwd_len = strlen(md5_pwd);
#if DEBUG
	warnx("md5(pwd)        : %s (%d)", md5_pwd, md5_pwd_len);
#endif
	
	/* Concat md5'ed passwd string and timestamp string. */
	int pwdts_len = md5_pwd_len + ts_len + 1;
	char pwdts[pwdts_len];
	memset(pwdts, 0, sizeof(pwdts));
	for (i = 0; i < md5_pwd_len; i++)
		pwdts[i] = md5_pwd[i];
	int j = md5_pwd_len;
	for (i = 0; i < ts_len; i++,j++)
		pwdts[j] = ts[i];
	pwdts[pwdts_len - 1] = '\0';
#if DEBUG
	warnx("md5(pwd)+ts     : %s (%d)", pwdts, strlen(pwdts));
#endif

	/* Compute md5 for md5'ed password + timestamp */
	char *md5_pwdts = md5(pwdts);
	if (md5_pwdts == NULL) {
		warnx("md5 computation for password + timestamp failed.");
		return -1;
	}
	int md5_pwdts_len = strlen(md5_pwdts);
#if DEBUG
	warnx("md5(md5(pwd)+ts): %s (%d)", md5_pwdts, md5_pwdts_len);
#endif

	/* Send md5'ed(md5'ed password + timestamp) to basex. */
	rc = send_db(sfd, md5_pwdts, md5_pwdts_len + 1);  // also send '\0'
	if (rc == -1) {
		warnx("Sending credentials failed.");
		return -1;
	}

	free(md5_pwd);
	free(md5_pwdts);

	/* Retrieve authentification status. */
	rc = basex_status(sfd);
	if (rc == -1) {
		warnx("Reading authentification status failed.");
		return -1;
	}
	if (rc != 0) {
		warnx("Authentification failed");
		return -1;
	}

#if DEBUG
	warnx("Authentification succeded.");
#endif
	return 0;
}

/**
 * Read status single byte from socket.
 */
int
basex_status(int sfd)
{
	char c;
	int b = read(sfd, &c, 1);	
	if (b == -1) {
		warnx("Can not retrieve status code.");
		return -1;
	}
	return c;
}

/**
 * Executes a command and returns a result string and an info/error string.
 *
 * A database command is sent to BaseX server connected on sfd.
 * The result is a \0 terminated, dynamically allocated string, which is placed
 * at the given result address or NULL.  The same holds for the processing
 * information stored at info.
 *
 * In either case it is the responsibility of the caller to free(3) those
 * strings.
 *
 * The returned int is 0 if the command could be processed successfully, in that
 * case the result contains the result string of the command and info holds
 * the processing information.
 * If a value >0 is returned, the command could not be processed successfully,
 * result contains NULL and info contains the database error message.
 * If -1 is interned, an error occurred, result and info are set to NULL.
 *
 *  int | result* | info* |
 * -----+---------+-------|
 *  -1  |  NULL   | NULL  |
 *   0  | result  | info  |
 *  >0  |  NULL   | error |
 *
 *  * strings shall be free(3)'ed by caller
 *
 * BaseX C/S protocol:
 *
 * client sends: {command} \0
 * server sends: {result}  \0 {info}  \0 \0
 *            or           \0 {error} \0 \1
 *
 * @param sfd socket file descriptor connected to BaseX server
 * @param command to be processed by BaseX server
 * @param result address at which result from BaseX server is placed
 * @param info address at which info/error message from BaseX server is placed
 * @return int 0 for success (result and info contain strings sent from BaseX)
 * -1 in case of failure (result and info are set to NULL), >0 an error occurred
 * while processing the command (result contains NULL, info contains error
 * message)
 */
int
basex_execute(int sfd, const char *command, char **result, char **info)
{
	int rc;

	/* Send {command}\0 to server. */
	rc = send_db(sfd, command, strlen(command) + 1);
	if (rc == -1) {
		warnx("Can not send command '%s' to server.", command);	
		goto err;
	}

	/* --- Receive from server:  {result} \0 {info}  \0 \0
	 *                                    \0 {error} \0 \1 */
	/* Receive {result} \0 */
	rc = readstring(sfd, result);
	if (rc == -1) {
		warnx("Can not retrieve result for command '%s' from server.", command);
		goto err;
	}
#if DEBUG
	warnx("[execute] result: '%s'\n", *result);
#endif

	/* Receive {info/error} \0 .*/
	rc = readstring(sfd, info);
	if (rc == -1) {
		warnx("Can not retrieve info for command '%s' from server.", *info);
		goto err;
	}
#if DEBUG
	warnx("[execute] info/error: '%s'\n", *info);
#endif

	/* Receive terminating \0 for success or \1 for error .*/
	rc = basex_status(sfd);
#if DEBUG
	warnx("[execute] status: '%d'\n", rc);
#endif
	if (rc == -1) {
		warnx("Can not retrieve status.");
		goto err;
	}
	if (rc == 1) {
		warnx("BaseX error message : %s", *info);
		free(*result);
		*result = NULL;
	}

	assert(rc == 0 || rc == 1);
	return rc;

err:
	*result = NULL;
	*info = NULL;
	return -1;
}

/**
 * Quits database session and closes stream connection to database server.
 *
 * @param socket file descriptor for database session.
 */
void
basex_close(int sfd)
{
	/* Send {exit}\0 to server. */
	int rc = send_db(sfd, "exit", 4 + 1);
	if (rc != 0)
		warnx("Can not send 'exit' command to server.");
		
	/* Close socket. */
	rc = shutdown(sfd, SHUT_RDWR);
	if (rc == -1)
		warn("Can not properly shutdown socket.");
}

/**
 * Writes buffer buf of buf_len to socket sfd.
 *
 * @param socket file descriptor for database session.
 * @param buf to be sent to server
 * @param buf_len # of bytes in buf
 * @return 0 if all data has successfully been written to server,
 *        -1 in case of failure.
 */
static int
send_db(int sfd, const char *buf, size_t buf_len)
{
	ssize_t ret;

	while (buf_len != 0 && (ret = write(sfd, buf, buf_len)) != 0) {
		if (ret == -1) {
			if (errno == EINTR)
				continue;
			warn("Can not write to server");
			return -1;
		}
#if DEBUG
		int i;
		warnx("write: \n");
		for (i = 0; i < ret; i++)
			warnx("[write] %3d : 0x%08x %4d %c", i, buf[i], buf[i], buf[i]);
#endif /* DEBUG */
		buf_len -= ret;
		buf += ret;
	}
	return 0;
}
