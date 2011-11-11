package org.basex.api.webdav;

import static org.basex.query.func.Function.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.basex.api.HTTPSession;
import org.basex.core.Text;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.io.in.ArrayInput;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Token;
import org.basex.util.Util;
import org.basex.util.list.StringList;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * Base class for all WebDAV resources.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXResource implements Resource {
  /** Date format of the "Time Stamp" field in INFO DB. */
  private static final String DATEFORMAT = "dd.MM.yyyy HH:mm:ss";
  /** File path separator. */
  static final char SEP = '/';
  /** Dummy file for empty folder.*/
  static final String DUMMY = ".empty";
  /** Database. */
  protected final String db;
  /** Resource path (without leading '/'). */
  protected final String path;
  /** Information on current session. */
  protected final HTTPSession session;
  /** Last modified date. */
  protected final Date mdate;

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param s current session
   */
  public BXResource(final String d, final String p, final HTTPSession s) {
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
  public BXResource(final String d, final String p, final long m,
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
    return Text.NAME;
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
   * List all databases.
   * @param s session
   * @return a list of database names
   * @throws IOException I/O exception
   */
  static StringList listDBs(final Session s) throws IOException {
    final StringList result = new StringList();
    final Query q = s.query(_DB_LIST.args());
    try {
      while(q.more()) result.add(q.next());
    } finally {
      q.close();
    }
    return result;
  }

  /**
   * Get a valid database name from a general file name.
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
   * Check a folder for a dummy document and delete it.
   * @param s active client session
   * @param db database name
   * @param p path
   * @return {@code true} if dummy document existed
   * @throws IOException I/O exception
   */
  static boolean deleteDummy(final Session s, final String db, final String p)
      throws IOException {

    final String dummy = p + SEP + DUMMY;
    if(!pathExists(s, db, dummy)) return false;

    // path contains dummy document
    s.execute(new Open(db));
    s.execute(new Delete(dummy));
    return true;
  }

  /**
   * Check if a folder is empty and create a dummy document.
   * @param s active client session
   * @param db database name
   * @param p path
   * @return {@code true} if dummy document was created
   * @throws IOException I/O exception
   */
  static boolean createDummy(final Session s, final String db, final String p)
      throws IOException {
    // check if path is a folder and is empty
    if(p.matches("[^/]") || pathExists(s, db, p)) return false;

    s.execute(new Open(db));
    s.store(p + SEP + DUMMY, new ArrayInput(Token.EMPTY));
    return true;
  }

  /**
   * Create a folder or document resource.
   * @param s active client session
   * @param d database name
   * @param p resource path
   * @param hs current session
   * @return requested resource, or {@code null} if it does not exist
   * @throws IOException I/O exception
   */
  static BXResource resource(final Session s, final String d, final String p,
      final HTTPSession hs) throws IOException {
    return exists(s, d, p) ? file(s, d, p, hs) :
      pathExists(s, d, p) ? folder(s, d, p, hs) : null;
  }

  /**
   * Create a file resource.
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
        "let $a := " + _DB_DETAILS.args("$d", "$p") +
        "return (" +
            "$a/@path/data()," +
            "$a/@raw/data()," +
            "$a/@content-type/data()," +
            "$a/@modified-date/data()," +
            "$a/@size/data())");
    q.bind("d", d);
    q.bind("p", p);
    try {
      final String path = stripLeadingSlash(q.next());
      final boolean raw = Boolean.parseBoolean(q.next());
      final String ctype = q.next();
      final long mod = Long.parseLong(q.next());
      final Long size = raw ? Long.valueOf(q.next()) : null;
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
    return new BXFolder(d, p, databaseTimestamp(s, d), hs);
  }

  /**
   * Create a database folder resource.
   * @param s active client session
   * @param d database name
   * @param hs current session
   * @return requested resource
   * @throws IOException I/O exception
   */
  static BXDatabase database(final Session s, final String d,
      final HTTPSession hs) throws IOException {
    return new BXDatabase(d, databaseTimestamp(s, d), hs);
  }

  /**
   * Retrieve the time stamp of a database.
   * @param s active session
   * @param d database name
   * @return database time stamp
   * @throws IOException I/O exception
   */
  private static Long databaseTimestamp(final Session s, final String d)
      throws IOException {
    final Query q = s.query(_DB_INFO.args("$p"));
    q.bind("p", d);
    final String inf = q.execute();
    // parse the timestamp
    final String ts = Text.INFOTIME + Text.COLS;
    final int p = inf.indexOf(ts);
    if(p >= 0) {
      final String dt = inf.substring(p + ts.length(), inf.indexOf(Text.NL, p));
      if(!dt.isEmpty()) {
        try {
          return new SimpleDateFormat(DATEFORMAT).parse(dt).getTime();
        } catch(final ParseException e) {
          Util.errln(e);
        }
      }
    }
    return null;
  }

  /**
   * Check if any resources start with the given path.
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
