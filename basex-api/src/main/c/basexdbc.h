/**
 * basexdbc.h : communicate with BaseX database server
 *
 * Copyright (c) 2005-12, Alexander Holupirek <alex@holupirek.de>, BSD license
 */

/* Connect to BaseX server and open session. Returns socket file descriptor. */
int basex_connect(const char *host, const char *port);

/* Authenticate for this session (passing socket desc, db user, and passwd). */
int basex_authenticate(int sfd, const char *user, const char *passwd);

/*  Send database command to server.
 *  Expect result and info to be filled (must be free(3)'ed afterwards).
 *
 *  int | result | info  |
 * -----+--------+-------|
 *  -1  |  NULL  | NULL  | general error (i/o and the like)
 *   0  | result | info  | database command has been processed successfully
 *  >0  |  NULL  | error | database command processing failed
 *
 * BaseX commands: http://docs.basex.org/wiki/Commands
 */
int basex_execute(int sfd, const char *command, char **result, char **info);

/* Close session with descriptor sfd. */
void basex_close(int sfd);
