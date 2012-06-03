package org.basex.http.webdav;

import static org.basex.query.func.Function.*;

import java.io.*;
import java.util.*;
import java.util.Date;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.http.*;
import org.basex.io.in.*;
import org.basex.query.value.item.*;
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
  /** HTTP context. */
  protected final HTTPContext http;
  /** Last modified date. */
  protected final Date mdate;

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param h http context
   */
  protected BXResource(final String d, final String p, final HTTPContext h) {
    this(d, p, -1, h);
  }

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param m last modification date
   * @param h http context
   */
  protected BXResource(final String d, final String p, final long m,
      final HTTPContext h) {

    db = d;
    path = stripLeadingSlash(p);
    mdate = m == -1 ? null : new Date(m);
    http = h;
  }

  @Override
  public Object authenticate(final String user, final String pass) {
    if(user != null) http.credentials(user, pass);
    return user;
  }

  @Override
  public boolean authorise(final Request request, final Method method, final Auth auth) {
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
   * Checks a folder for a dummy document and delete it.
   * @param p path
   * @throws IOException I/O exception
   */
  void deleteDummy(final String p) throws IOException {
    final String dummy = p + SEP + DUMMY;
    if(!pathExists(db, dummy, http)) return;

    // path contains dummy document
    final Session session = http.session();
    session.execute(new Open(db));
    session.execute(new Delete(dummy));
  }

  /**
   * Checks if a folder is empty and create a dummy document.
   * @param p path
   * @throws IOException I/O exception
   */
  void createDummy(final String p) throws IOException {
    // check if path is a folder and is empty
    if(p.matches("[^/]") || pathExists(db, p, http)) return;

    final Session session = http.session();
    session.execute(new Open(db));
    session.store(p + SEP + DUMMY, new ArrayInput(Token.EMPTY));
  }

  /**
   * Checks if the specified database exists.
   * @param db database to be found
   * @param http http context
   * @return result of check
   * @throws IOException I/O exception
   */
  static boolean dbExists(final String db, final HTTPContext http) throws IOException {
    final Query q = http.session().query(_DB_LIST.args() + "[. = $db]");
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
   * @param db name of database
   * @return valid database name
   */
  static String dbname(final String db) {
    final int i = db.lastIndexOf('.');
    return (i < 0 ? db : db.substring(0, i)).replaceAll("[^\\w-]", "");
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
   * Creates a folder or document resource.
   * @param db name of database
   * @param path resource path
   * @param http http context
   * @return requested resource, or {@code null} if it does not exist
   * @throws IOException I/O exception
   */
  static BXResource resource(final String db, final String path, final HTTPContext http)
      throws IOException {

    return exists(db, path, http) ? file(db, path, http) : pathExists(db, path, http) ?
        folder(db, path, http) : null;
  }

  /**
   * Creates a file resource.
   * @param db name of database
   * @param path resource path
   * @param http http context
   * @return requested resource, or {@code null} if it does not exist
   * @throws IOException I/O exception
   */
  static BXFile file(final String db, final String path, final HTTPContext http)
      throws IOException {

    final Query q = http.session().query(
        "let $a := " + _DB_LIST_DETAILS.args("$d", "$p") +
        "return (" +
            "$a/@raw/data()," +
            "$a/@content-type/data()," +
            "$a/@modified-date/data()," +
            "$a/@size/data()," +
            "$a/text())");
    q.bind("d", db);
    q.bind("p", path);
    try {
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = Dtm.parse(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
      final String pth = stripLeadingSlash(q.next());
      return new BXFile(db, pth, mod, raw, ctype, size, http);
    } finally {
      q.close();
    }
  }

  /**
   * Create a folder resource.
   * @param db name of database
   * @param path resource path
   * @param http http context
   * @return requested resource
   * @throws IOException I/O exception
   */
  static BXFolder folder(final String db, final String path, final HTTPContext http)
      throws IOException {
    return new BXFolder(db, path, timestamp(db, http), http);
  }

  /**
   * Creates database folder resource.
   * @param db name of database
   * @param http http context
   * @return requested resource
   * @throws IOException I/O exception
   */
  static BXDatabase database(final String db, final HTTPContext http) throws IOException {
    return new BXDatabase(db, timestamp(db, http), http);
  }

  /**
   * Checks if any of the resources start with the given path.
   * @param db name of database
   * @param path path
   * @param http http context
   * @return number of documents
   * @throws IOException I/O exception
   */
  static boolean pathExists(final String db, final String path, final HTTPContext http)
      throws IOException {

    final Query q = http.session().query(COUNT.args(_DB_LIST.args("$d", "$p")));
    q.bind("d", db);
    q.bind("p", path);
    return !q.execute().equals("0");
  }

  /**
   * Checks if any resource with the specified name exists.
   * @param db name of database
   * @param path resource path
   * @param http http context
   * @return number of documents
   * @throws IOException I/O exception
   */
  private static boolean exists(final String db, final String path,
      final HTTPContext http) throws IOException {

    final Query q = http.session().query(_DB_EXISTS.args("$d", "$p"));
    q.bind("d", db);
    q.bind("p", path);
    return q.execute().equals(Text.TRUE);
  }

  /**
   * Retrieves the time stamp of a database.
   * @param db name of database
   * @param http http context
   * @return database time stamp
   * @throws IOException I/O exception
   */
  private static long timestamp(final String db, final HTTPContext http)
      throws IOException {

    final String s = DATA.args(_DB_INFO.args("$p") + "/descendant::" + TIME + "[1]");
    final Query q = http.session().query(s);
    q.bind("p", db);
    try {
      // retrieve and parse timestamp
      return Util.parseDate(q.execute(), InfoDB.DATE).getTime();
    } catch(final Exception ex) {
      Util.errln(ex);
      return 0;
    }
  }
}
