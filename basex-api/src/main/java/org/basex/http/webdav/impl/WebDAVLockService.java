package org.basex.http.webdav.impl;

import org.basex.core.BaseXException;
import org.basex.data.Result;
import org.basex.http.HTTPContext;
import org.basex.io.IOStream;
import org.basex.io.out.ArrayOutput;
import org.basex.io.serial.Serializer;
import org.basex.query.QueryException;
import org.basex.query.QueryProcessor;
import org.basex.util.Util;
import org.basex.util.list.StringList;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.basex.http.webdav.impl.Utils.SEP;
import static org.basex.util.Token.string;

/**
 * Service managing the WebDAV locks.
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVLockService {
  /** Name of the database with the WebDAV locks. */
  static final String WEBDAV_LOCKS_DB = "~webdav";
  /** HTTP context. */
  private final HTTPContext http;

  /**
   * Constructor.
   * @param h HTTP context
   */
  WebDAVLockService(final HTTPContext h) {
    http = h;
  }

  /**
   * Releases the lock for the given token.
   * @param token lock token
   * @throws java.io.IOException I/O exception
   */
  public void unlock(final String token) throws IOException {
    new LockQuery(http, "w:delete-lock($lock-token)").
      bind("lock-token", token).
      execute();
  }

  /**
   * Renews the lock with the given token.
   * @param token lock token
   * @throws IOException I/O exception
   */
  public void refreshLock(final String token) throws IOException {
    new LockQuery(http, "w:refresh-lock($lock-token)").
      bind("lock-token", token).
      execute();
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
  public String lock(final String db, final String p, final String scope,
      final String type, final String depth, final String user, final Long timeout)
      throws IOException {

    initLockDb();
    final String token = UUID.randomUUID().toString();
    new LockQuery(http, "w:create-lock(" +
      "$path," +
      "$lock-token," +
      "$lock-scope," +
      "$lock-type," +
      "$lock-depth," +
      "$lock-owner," +
      "$lock-timeout)").
      bind("path", db + SEP + p).
      bind("lock-token", token).
      bind("lock-scope", scope).
      bind("lock-type", type).
      bind("lock-depth", depth).
      bind("lock-owner", user).
      bind("lock-timeout", timeout == null ? Long.MAX_VALUE : timeout).
      execute();
    return token;
  }

  /**
   * Gets lock with given token.
   * @param token lock token
   * @return lock
   * @throws IOException I/O exception
   */
  public String lock(final String token) throws IOException {
    final StringList locks = new LockQuery(http, "w:lock($lock-token)").
      bind("$lock-token", token).
      execute();
    return locks.isEmpty() ? null : locks.get(0);
  }

  /**
   * Gets active locks for the given resource.
   * @param db database
   * @param p path
   * @return locks
   * @throws IOException I/O exception
   */
  public String lock(final String db, final String p) throws IOException {
    final StringList locks = new LockQuery(http, "w:locks-on($path)").
      bind("path", db + SEP + p).
      execute();
    return locks.isEmpty() ? null : locks.get(0);
  }

  /**
   * Checks if there are active conflicting locks for the given resource.
   * @param db database
   * @param p path
   * @return {@code true} if there active conflicting locks
   * @throws IOException I/O exception
   */
  public boolean conflictingLocks(final String db, final String p) throws IOException {
    return new LockQuery(http,
      "w:conflicting-locks(" +
        "<w:lockinfo>" +
        "<w:path>{ $path }</w:path>" +
        "<w:scope>exclusive</w:scope>" +
        "<w:depth>infinite</w:depth>" +
        "<w:owner>{ $owner }</w:owner>" +
        "</w:lockinfo>)").
      bind("path", db + SEP + p).
      bind("owner", http.user).
      execute().
      size() > 0;
  }

  /**
   * Creates the lock database, if it does not exist.
   * @throws IOException I/O exception
   */
  private void initLockDb() throws IOException {
    new LockQuery(http, "w:init-lock-db()").execute();
  }

  /** Class abstracting the underlying query mechanism. */
  private static final class LockQuery {
    /** Query processor. */
    private final QueryProcessor p;

    /**
     * Constructor.
     * @param h HTTP context
     * @param q query  text
     */
    LockQuery(final HTTPContext h, final String q) {
      p = new QueryProcessor(q, h.context());
    }

    /**
     * Binds a new query parameter.
     * @param n parameter name
     * @param v parameter value
     * @return {@code this}
     * @throws IOException error during binding
     */
    LockQuery bind(final String n, final Object v) throws IOException {
      try {
        p.bind(n, v);
      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      }
      return this;
    }

    /**
     * Executes the query.
     * @return list of serialized result items
     * @throws IOException error during query execution
     */
    StringList execute() throws IOException {
      registerModule();
      try {
        final Result r = p.execute();
        final int n = (int) r.size();
        final StringList items = new StringList(n);
        for(int i = 0; i < n; i++) {
          final ArrayOutput o = new ArrayOutput();
          r.serialize(Serializer.get(o), 0);
          items.add(o.toString());
        }
        return items;
      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      } catch(final Exception ex) {
        Util.debug(ex);
        throw new BaseXException(ex);
      } finally {
        p.close();
      }
    }

    /**
     * Registers the WebDAV XQuery module.
     * @throws IOException error during parsing the module
     */
    private void registerModule() throws IOException {
      try {
        p.ctx.parseLibrary(string(readModule()), null, p.sc);
      } catch(final QueryException ex) {
        throw new BaseXException(ex);
      }
    }

    /**
     * Reads the WebDAV XQuery module from the classpath.
     * @return the content of the module
     * @throws IOException error during reading the module
     */
    private byte[] readModule() throws IOException {
      final ClassLoader cl = getClass().getClassLoader();
      final InputStream s = cl.getResourceAsStream("xquery/webdav.xqm");
      if(s == null) throw new IOException("WebDAV module not found");
      return new IOStream(s).read();
    }
  }
}
