package org.basex.api.webdav;

import static java.lang.Integer.*;

import java.io.IOException;
import java.util.Date;

import org.basex.api.HTTPSession;
import org.basex.core.Text;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Close;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Util;
import org.basex.util.list.StringList;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.Resource;

/**
 * Base class for all WebDAV resources.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXResource implements Resource {
  /** File path separator. */
  static final char SEP = '/';
  /** XML mime type. */
  static final String MIMETYPEXML = "application/xml";
  /** Zip mime type. */
  static final String MIMETYPEZIP = "application/zip";
  /** Dummy xml file.*/
  static final String DUMMY = ".empty";
  /** Dummy xml content.*/
  static final String DUMMYCONTENT = "<empty/>";
  /** Database. */
  protected final String db;
  /** Resource path (without leading '/'). */
  protected final String path;
  /** Information on current session. */
  protected final HTTPSession session;

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param s current session
   */
  public BXResource(final String d, final String p, final HTTPSession s) {
    db = d;
    path = stripLeadingSlash(p);
    session = s;
  }

  @Override
  public Object authenticate(final String u, final String p) {
    if(u != null) {
      session.user = u;
      session.pass = p;
    }
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
    return null;
  }

  /**
   * List all databases.
   * @param s session
   * @return a list of database names
   * @throws IOException I/O exception
   */
  static StringList listDBs(final Session s) throws IOException {
    final StringList result = new StringList();
    final Query q = s.query("db:list()");
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
   * Check if content type is supported.
   * @param ctype content type
   * @return {@code true} if BaseX can handle the content type
   */
  static boolean supported(final String ctype) {
    // [DP] additional content types can be supported in the future
    return ctype != null && ctype.indexOf(MIMETYPEXML) >= 0;
  }

  /**
   * Prints exception message to standard error.
   * @param ex exception
   * @return bad request exception
   * @throws BadRequestException bad request exception
   */
  BadRequestException error(final Exception ex) throws BadRequestException {
    Util.errln(ex);
    throw new BadRequestException(this, ex.getMessage());
  }

  /**
   * Count documents which paths start with the given path.
   * @param s active client session
   * @param db database
   * @param path path
   * @return number of documents
   * @throws IOException I/O exception
   */
  static int count(final Session s, final String db, final String path)
      throws IOException {
    final Query q = s.query("count(db:list($d, $p))");
    q.bind("$d", db);
    q.bind("$p", path);
    return parseInt(q.execute());
  }

  /**
   * Checks if a document with the specified name exists.
   * @param s active client session
   * @param db database name
   * @param p resource path
   * @return number of documents
   * @throws IOException I/O exception
   */
  static boolean exists(final Session s, final String db, final String p)
      throws IOException {

    final String path = stripLeadingSlash(p);
    final Query q = s.query("exists(db:list($d, $p)[. = $p])");
    q.bind("$d", db);
    q.bind("$p", path);
    return Boolean.parseBoolean(q.execute());
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
    if(count(s, db, dummy) == 0) return false;

    // path contains dummy document
    s.execute(new Open(db));
    s.execute(new Delete(dummy));
    s.execute(new Close());
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
    // check, if path is a folder and is empty
    if(p.matches("[^/]") || count(s, db, p) > 0) return false;

    s.execute(new Open(db));
    s.execute(new Add(DUMMYCONTENT, DUMMY, p));
    s.execute(new Close());
    return true;
  }

  /**
   * Create a folder or document resource.
   * @param s active client session
   * @param db database name
   * @param path resource path
   * @param hs current session
   * @return requested resource or {@code null} if it does not exist
   */
  static BXResource resource(final Session s, final String db,
      final String path, final HTTPSession hs) {

    try {
      // check if there is a document in the collection having this path
      if(exists(s, db, path)) return new BXDocument(db, path, hs);

      // check if there are paths in the collection starting with this path
      if(count(s, db, path) > 0) return new BXFolder(db, path, hs);
    } catch(final IOException ex) {
      Util.errln(ex);
    }
    return null;
  }
}
