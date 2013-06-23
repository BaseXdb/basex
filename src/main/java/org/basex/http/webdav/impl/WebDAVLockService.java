package org.basex.http.webdav.impl;

import org.basex.http.HTTPContext;
import org.basex.server.Query;

import java.io.IOException;
import java.util.UUID;

import static org.basex.http.webdav.impl.Utils.SEP;

/**
 * Service managing the WebDAV locks.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public class WebDAVLockService {
  /** Name of the database with the WebDAV locks. */
  static final String WEBDAV_LOCKS_DB = "webdav-locks";
  /** HTTP context. */
  private final HTTPContext http;

  /**
   * Constructor.
   * @param h HTTP context
   */
  WebDAVLockService(HTTPContext h) {
    http = h;
  }

  /**
   * Release the lock for the given token.
   * @param token lock token
   * @throws java.io.IOException I/O exception
   */
  public void unlock(final String token) throws IOException {
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:delete-lock($lock-token)");
    q.bind("lock-token", token);
    q.execute();
  }

  /**
   * Renew the lock with the given token.
   * @param token lock token
   * @throws IOException I/O exception
   */
  public void refreshLock(final String token) throws IOException {
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:refresh-lock($lock-token)");
    q.bind("lock-token", token);
    q.execute();
  }

  /**
   * Create a new lock for the specified resource.
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
  public String lock(final String db, final String p, final String scope,
                     final String type, final String depth, final String user, final Long timeout) throws
    IOException {
    initLockDb();
    final String token = UUID.randomUUID().toString();
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:create-lock(" +
      "$resource," +
      "$lock-token," +
      "$lock-scope," +
      "$lock-type," +
      "$lock-depth," +
      "$lock-owner," +
      "$lock-timeout)");
    q.bind("resource", db + SEP + p);
    q.bind("lock-token", token);
    q.bind("lock-scope", scope);
    q.bind("lock-type", type);
    q.bind("lock-depth", depth);
    q.bind("lock-owner", user);
    q.bind("lock-timeout", timeout == null ? Long.MAX_VALUE : timeout);

    q.execute();
    return token;
  }

  /**
   * Get lock with given token.
   * @param token lock token
   * @return lock
   * @throws IOException I/O exception
   */
  public String lock(final String token) throws IOException {
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:get-lock($lock-token)");
    q.bind("$lock-token", token);

    return q.next();
  }

  /**
   * Get active locks for the given resource.
   * @param db database
   * @param p path
   * @return locks
   * @throws IOException I/O exception
   */
  public String lock(final String db, final String p) throws IOException {
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:get-locks-on($resource)");
    q.bind("resource", db + SEP + p);

    return q.next();
  }

  /**
   * Check if there are active conflicting locks for the given resource.
   * @param db database
   * @param p path
   * @return {@code true} if there active conflicting locks
   * @throws IOException I/O exception
   */
  public boolean conflictingLocks(final String db, final String p) throws IOException {
    final Query q = http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:get-conflicting-locks(" +
        "<w:lockinfo>" +
        "<w:path>{ $resource }</w:path>" +
        "<w:scope>exclusive</w:scope>" +
        "<w:depth>infinite</w:depth>" +
        "<w:owner>{ $lock-owner }</w:owner>" +
        "</w:lockinfo>)");
    q.bind("resource", db + SEP + p);
    q.bind("lock-owner", http.user);

    return q.more();
  }

  /**
   * Creates the lock database, if it does not exist.
   * @throws IOException I/O exception
   */
  private void initLockDb() throws IOException {
    http.session().query(
      "import module namespace w = 'http://basex.org/webdav';" +
      "w:init-lock-db()").execute();
  }
}
