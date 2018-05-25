package org.basex.http.webdav;

import static org.basex.http.webdav.WebDAVUtils.*;

import java.io.*;
import java.util.*;

import org.basex.http.*;
import org.basex.query.value.*;
import org.basex.query.value.seq.*;

/**
 * Service managing the WebDAV locks.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Dimitar Popov
 */
final class WebDAVLockService {
  /** HTTP connection. */
  private final HTTPConnection conn;

  /**
   * Constructor.
   * @param conn HTTP connection
   */
  WebDAVLockService(final HTTPConnection conn) {
    this.conn = conn;
  }

  /**
   * Releases the lock for the given token.
   * @param token lock token
   * @throws IOException I/O exception
   */
  void unlock(final String token) throws IOException {
    new WebDAVQuery("w:unlock($token)").lock().bind("token", token).execute(conn);
  }

  /**
   * Renews the lock with the given token.
   * @param token lock token
   * @throws IOException I/O exception
   */
  void refreshLock(final String token) throws IOException {
    new WebDAVQuery("w:refresh-lock($token)").lock().bind("token", token).execute(conn);
  }

  /**
   * Creates a new lock for the specified resource.
   * @param db database
   * @param p path
   * @param scope lock scope
   * @param type lock type
   * @param depth lock depth
   * @param user lock user
   * @param timeout lock timeout
   * @return lock token
   * @throws IOException I/O exception
   */
  String lock(final String db, final String p, final String scope, final String type,
      final String depth, final String user, final Long timeout) throws IOException {

    final String token = UUID.randomUUID().toString();
    final WebDAVQuery query = new WebDAVQuery("w:create-lock(" +
        "$path, $token, $scope, $type, $depth, $user, $timeout)").lock();
    query.bind("path", db + SEP + p);
    query.bind("token", token);
    query.bind("scope", scope);
    query.bind("type", type);
    query.bind("depth", depth);
    query.bind("user", user);
    final long t = timeout == null ? Long.MAX_VALUE : timeout.longValue();
    query.bind("timeout", Long.toString(Math.min(31700000, t)));
    query.execute(conn);
    return token;
  }

  /**
   * Gets lock with given token.
   * @param token lock token
   * @return lock or empty sequence
   * @throws IOException I/O exception
   */
  Value locks(final String token) throws IOException {
    return WebDAVQuery.hasLocks() ?
      new WebDAVQuery("w:lock($token)").lock().bind("token", token).execute(conn) : Empty.SEQ;
  }

  /**
   * Gets active lock for the given resource.
   * @param db database
   * @param path path
   * @return lock or empty sequence
   * @throws IOException I/O exception
   */
  Value locks(final String db, final String path) throws IOException {
    return WebDAVQuery.hasLocks() ? new WebDAVQuery("w:locks-on($path)").lock().
      bind("path", db + SEP + path).execute(conn) : Empty.SEQ;
  }

  /**
   * Checks if there are active conflicting locks for the given resource.
   * @param db database
   * @param p path
   * @return {@code true} if there are conflicting locks
   * @throws IOException I/O exception
   */
  boolean conflictingLocks(final String db, final String p) throws IOException {
    if(!WebDAVQuery.hasLocks()) return false;

    final WebDAVQuery query = new WebDAVQuery("w:conflicting-locks(" +
      "<lockinfo>" +
        "<path>{ $path }</user>" +
        "<scope>exclusive</user>" +
        "<depth>infinity</user>" +
        "<user>{ $user }</user>" +
      "</lockinfo>)").lock();
    query.bind("path", db + SEP + p).bind("user", conn.context.user().name());
    return !query.execute(conn).isEmpty();
  }
}
