package org.basex.api.webdav;

import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.io.in.*;
import org.basex.query.func.*;
import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.http.*;
import com.bradmcevoy.http.Request.Method;

/**
 * Base class for all WebDAV resources.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXResource implements Resource {
  /** Time string. */
  static final String TIME =
      Text.TIMESTAMP.replaceAll(" |-", "").toLowerCase(Locale.ENGLISH);
  /** File path separator. */
  static final char SEP = '/';
  /** Dummy file for empty folder.*/
  static final String DUMMY = ".empty";
  /** Database. */
  protected final String db;
  /** Resource path (without leading '/'). */
  protected final String path;
  /** HTTP session reference. */
  protected final HTTPSession session;
  /** Last modified date. */
  protected final Date mdate;

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param s current session
   */
  protected BXResource(final String d, final String p, final HTTPSession s) {
    db = d;
    path = stripLeadingSlash(p);
    mdate = null;
    session = s;
  }

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param m last modification date
   * @param s current session
   */
  protected BXResource(final String d, final String p, final long m,
      final HTTPSession s) {
    db = d;
    path = stripLeadingSlash(p);
    mdate = new Date(m);
    session = s;
  }

  @Override
  public Object authenticate(final String u, final String p) {
    if(u != null) session.update(u, p);
    return u;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    return auth != null && auth.getTag() != null;
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return Prop.NAME;
  }

  @Override
  public String getUniqueId() {
    return null;
  }

  @Override
  public String getName() {
    final int idx = path.lastIndexOf(SEP);
    return idx < 0 ? path : path.substring(idx + 1, path.length());
  }

  @Override
  public Date getModifiedDate() {
    return mdate;
  }

  /**
   * Checks if the specified database exists.
   * @param s session
   * @param db database to be found
   * @return result of check
   * @throws IOException I/O exception
   */
  static boolean dbExists(final Session s, final String db)
      throws IOException {

    final Query q = s.query(_DB_LIST.args() + "[. = $db]");
    q.bind("db", db);
    try {
      if(q.more()) return true;
    } finally {
      q.close();
    }
    return false;
  }

  /**
   * Gets a valid database name from a general file name.
   * @param n name
   * @return valid database name
   */
  static String dbname(final String n) {
    final int i = n.lastIndexOf('.');
    return (i < 0 ? n : n.substring(0, i)).replaceAll("[^\\w-]", "");
  }

  /**
   * String leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  static String stripLeadingSlash(final String s) {
    return s == null || s.isEmpty() || s.charAt(0) != SEP ? s : s.substring(1);
  }

  /**
   * Checks a folder for a dummy document and delete it.
   *
   * @param s active client session
   * @param db database name
   * @param p path
   * @throws IOException I/O exception
   */
  static void deleteDummy(final Session s, final String db, final String p)
      throws IOException {

    final String dummy = p + SEP + DUMMY;
    if(!pathExists(s, db, dummy)) return;

    // path contains dummy document
    s.execute(new Open(db));
    s.execute(new Delete(dummy));
  }

  /**
   * Checks if a folder is empty and create a dummy document.
   * @param s active client session
   * @param db database name
   * @param p path
   * @throws IOException I/O exception
   */
  static void createDummy(final Session s, final String db, final String p)
      throws IOException {
    // check if path is a folder and is empty
    if(p.matches("[^/]") || pathExists(s, db, p)) return;

    s.execute(new Open(db));
    s.store(p + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }

  /**
   * Creates a folder or document resource.
   * @param s active client session
   * @param d database name
   * @param p resource path
   * @param hs current session
   * @return requested resource, or {@code null} if it does not exist
   * @throws IOException I/O exception
   */
  static BXResource resource(final Session s, final String d, final String p,
      final HTTPSession hs) throws IOException {

    return exists(s, d, p) ? file(s, d, p, hs) : pathExists(s, d, p) ?
        folder(s, d, p, hs) : null;
  }

  /**
   * Creates a file resource.
   * @param s active client session
   * @param d database name
   * @param p resource path
   * @param hs current session
   * @return requested resource, or {@code null} if it does not exist
   * @throws IOException I/O exception
   */
  static BXFile file(final Session s, final String d, final String p,
      final HTTPSession hs) throws IOException {
    final Query q = s.query(
        "let $a := " + _DB_LIST_DETAILS.args("$d", "$p") +
        "return (" +
            "$a/@raw/data()," +
            "$a/@content-type/data()," +
            "$a/@modified-date/data()," +
            "$a/@size/data()," +
            "$a/text())");
    q.bind("d", d);
    q.bind("p", p);
    try {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = FNDb.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String path = stripLeadingSlash(q.next());
      return new BXFile(d, path, mod, raw, ctype, size, hs);
    } finally {
      q.close();
    }
  }

  /**
   * Create a folder resource.
   * @param s active client session
   * @param d database name
   * @param p resource path
   * @param hs current session
   * @return requested resource
   * @throws IOException I/O exception
   */
  static BXFolder folder(final Session s, final String d, final String p,
      final HTTPSession hs) throws IOException {
    return new BXFolder(d, p, timestamp(s, d), hs);
  }

  /**
   * Creates a database folder resource.
   * @param s active client session
   * @param hs current session
   * @return requested resource
   * @throws IOException I/O exception
   */
  static List<BXResource> databases(final Session s, final HTTPSession hs)
      throws IOException {

    final List<BXResource> dbs = new ArrayList<BXResource>();
    final Query q = s.query("for $d in " + _DB_LIST_DETAILS.args() +
        "return ($d/text(), $d/@modified-date/data())");
    try {
      while(q.more()) {
        final String name = q.next();
        final long mod = FNDb.parse(q.next());
        dbs.add(new BXDatabase(name, mod, hs));
      }
    } catch(final Exception ex) {
      Util.errln(ex);
    } finally {
      q.close();
    }
    return dbs;
  }

  /**
   * Creates database folder resource.
   * @param s active client session
   * @param d database name
   * @param hs current session
   * @return requested resource
   * @throws IOException I/O exception
   */
  static BXDatabase database(final Session s, final String d, final HTTPSession hs)
      throws IOException {
    return new BXDatabase(d, timestamp(s, d), hs);
  }

  /**
   * Retrieves the time stamp of a database.
   * @param s active session
   * @param d database name
   * @return database time stamp
   * @throws IOException I/O exception
   */
  private static long timestamp(final Session s, final String d)
      throws IOException {

    final Query q = s.query(DATA.args(_DB_INFO.args("$p") +
        "/descendant::" + TIME + "[1]"));
    q.bind("p", d);
    try {
      // retrieve and parse timestamp
      return InfoDB.DATE.parse(q.execute()).getTime();
    } catch(final Exception ex) {
      Util.errln(ex);
      return 0;
    }
  }

  /**
   * Checks if any of the resources start with the given path.
   * @param s active client session
   * @param d database
   * @param p path
   * @return number of documents
   * @throws IOException I/O exception
   */
  static boolean pathExists(final Session s, final String d, final String p)
      throws IOException {
    final Query q = s.query(COUNT.args(_DB_LIST.args("$d", "$p")));
    q.bind("d", d);
    q.bind("p", p);
    return !q.execute().equals("0");
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param s active client session
   * @param d database name
   * @param p resource path
   * @return number of documents
   * @throws IOException I/O exception
   */
  private static boolean exists(final Session s, final String d, final String p)
      throws IOException {
    final Query q = s.query(_DB_EXISTS.args("$d", "$p"));
    q.bind("d", d);
    q.bind("p", p);
    return q.execute().equals(Text.TRUE);
  }
}
