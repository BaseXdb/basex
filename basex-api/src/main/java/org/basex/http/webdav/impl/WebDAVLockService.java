package org.basex.http.webdav.impl;

import static org.basex.http.webdav.impl.Utils.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.Map.Entry;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.list.*;

/**
 * Service managing the WebDAV locks.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVLockService {
  /** Path to WebDAV module. */
  private static final String FILE = "xquery/webdav.xqm";
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
   * @throws IOException I/O exception
   */
  public void unlock(final String token) throws IOException {
    execute(new WebDAVQuery("w:delete-lock($token)").bind("token", token));
  }

  /**
   * Renews the lock with the given token.
   * @param token lock token
   * @throws IOException I/O exception
   */
  public void refreshLock(final String token) throws IOException {
    execute(new WebDAVQuery("w:refresh-lock($token)").bind("token", token));
  }

  /**
   * Creates a new lock for the specified resource.
   * @param db database
   * @param p path
   * @param scope lock scope
   * @param type lock type
   * @param depth lock depth
   * @param user lock user
   * @param to lock timeout
   * @return lock token
   * @throws IOException I/O exception
   */
  public String lock(final String db, final String p, final String scope, final String type,
      final String depth, final String user, final Long to) throws IOException {

    initLockDb();
    final String token = UUID.randomUUID().toString();

    final WebDAVQuery query = new WebDAVQuery("w:create-lock(" +
        "$path, $token, $scope, $type, $depth, $owner, $timeout)");
    query.bind("path", db + SEP + p);
    query.bind("token", token);
    query.bind("scope", scope);
    query.bind("type", type);
    query.bind("depth", depth);
    query.bind("owner", user);
    query.bind("timeout", to == null ? Long.MAX_VALUE : to);
    execute(query);
    return token;
  }

  /**
   * Gets lock with given token.
   * @param token lock token
   * @return lock
   * @throws IOException I/O exception
   */
  public String lock(final String token) throws IOException {
    final StringList locks = execute(new WebDAVQuery("w:lock($token)").bind("token", token));
    return locks.isEmpty() ? null : locks.get(0);
  }

  /**
   * Gets active locks for the given resource.
   * @param db database
   * @param path path
   * @return locks
   * @throws IOException I/O exception
   */
  public String lock(final String db, final String path) throws IOException {
    final WebDAVQuery query = new WebDAVQuery("w:locks-on($path)").bind("path", db + SEP + path);
    final StringList sl = execute(query);
    return sl.isEmpty() ? null : sl.get(0);
  }

  /**
   * Checks if there are active conflicting locks for the given resource.
   * @param db database
   * @param p path
   * @return {@code true} if there active conflicting locks
   * @throws IOException I/O exception
   */
  public boolean conflictingLocks(final String db, final String p) throws IOException {
    return execute(new WebDAVQuery("w:conflicting-locks(" +
        "<w:lockinfo>" +
        "<w:path>{ $path }</w:path>" +
        "<w:scope>exclusive</w:scope>" +
        "<w:depth>infinite</w:depth>" +
        "<w:owner>{ $owner }</w:owner>" +
        "</w:lockinfo>)").bind("path", db + SEP + p).bind("owner", http.user)).isEmpty();
  }

  /**
   * Creates the lock database, if it does not exist.
   * @throws IOException I/O exception
   */
  private void initLockDb() throws IOException {
    execute(new WebDAVQuery("w:init-lock-db()"));
  }

  /**
   * Executes a query.
   * @param query query to be executed
   * @return list of serialized result items
   * @throws IOException error during query execution
   */
  private StringList execute(final WebDAVQuery query) throws IOException {
    final ClassLoader cl = getClass().getClassLoader();
    final InputStream s = cl.getResourceAsStream(FILE);
    if(s == null) throw new IOException("WebDAV module not found");
    final byte[] module = new IOStream(s).read();

    final QueryProcessor qp = new QueryProcessor(query.toString(), http.context());
    try {
      for(final Entry<String, Object> entry : query.entries()) {
        qp.bind(entry.getKey(), entry.getValue());
      }
      qp.qc.parseLibrary(string(module), FILE, qp.sc);

      final StringList items = new StringList();
      final ArrayOutput ao = new ArrayOutput();
      final Serializer ser = qp.getSerializer(ao);
      for(final Item it : qp.value()) {
        ser.serialize(it);
        items.add(ao.toString());
        ao.reset();
      }
      return items;
    } catch(final QueryException ex) {
      throw new BaseXException(ex);
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new BaseXException(ex);
    } finally {
      qp.close();
    }
  }
}
