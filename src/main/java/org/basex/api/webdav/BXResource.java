package org.basex.api.webdav;

import static java.lang.Integer.*;

import java.util.Date;
import org.basex.core.BaseXException;
import org.basex.core.cmd.Add;
import org.basex.core.cmd.Delete;
import org.basex.core.cmd.Open;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.list.StringList;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;

/**
 * Base class for all WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public abstract class BXResource implements Resource {
  /** File path separator. */
  static final char SEP = '/';
  /** XML mime type. */
  static final String MIMETYPEXML = "text/xml";
  /** Zip mime type. */
  static final String MIMETYPEZIP = "application/zip";
  /** Dummy xml file.*/
  static final String DUMMY = "EMPTY.xml";
  /** Dummy xml content.*/
  static final String DUMMYCONTENT = "<empty/>";
  /** User name. */
  protected String user;
  /** User password. */
  protected String pass;
  /** Database. */
  protected final String db;
  /** Resource path (without leading '/'). */
  protected final String path;
  /** Reference to the resource factory. */
  protected final BXResourceFactory factory;

  /**
   * Constructor.
   * @param d database name
   * @param p resource path
   * @param f resource factory
   */
  public BXResource(final String d, final String p, final BXResourceFactory f) {
    db = d;
    path = stripLeadingSlash(p);
    factory = f;
  }

  @Override
  public Object authenticate(final String u, final String p) {
    if(u != null) {
      this.user = u;
      this.pass = p;
    }
    return u;
  }

  @Override
  public boolean authorise(final Request request, final Method method,
      final Auth auth) {
    if(auth != null) {
      final String u = (String) auth.getTag();
      // [DP] WebDAV: check if user has sufficient privileges
      if(u != null) return true;
    }
    return false;
  }

  @Override
  public String checkRedirect(final Request request) {
    return null;
  }

  @Override
  public String getRealm() {
    return "BaseX";
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
   * Delete the document.
   * @param s current session
   * @throws BaseXException database exception
   */
  protected void delete(final Session s) throws BaseXException {
    s.execute(new Open(db));
    s.execute(new Delete(path));
  }

  /**
   * List all databases.
   * @param s session
   * @return a list of database names
   * @throws BaseXException query exception
   */
  static StringList listDbs(final Session s)
      throws BaseXException {
    final StringList result = new StringList();
    final Query q = s.query("db:list()");
    while(q.more()) result.add(q.next());
    return result;
  }

  /**
   * Get a valid database name from a general file name.
   * @param n name
   * @return valid database name
   */
  static String dbname(final String n) {
    final int i = n.lastIndexOf('.');
    return (i != -1 ? n.substring(0, i) : n).replaceAll("[^\\w-]", "");
  }

  /**
   * String leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  static String stripLeadingSlash(final String s) {
    return s != null && s.length() > 0 && s.charAt(0) == SEP ?
        s.substring(1) : s;
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
   * Count the number of documents starting with a certain prefix.
   * @param s active client session
   * @param db database name
   * @param path resource path
   * @return number of documents
   * @throws BaseXException query exception
   */
  static int count(final Session s, final String db, final String path)
      throws BaseXException {
    final String dbpath = db + SEP + path;
    final Query q = s.query("declare variable $d as xs:string external; "
        + "declare variable $p as xs:string external; "
        + "count(db:list($d)[starts-with(., $p)])");
    q.bind("$d", dbpath);
    q.bind("$p", path);
    return parseInt(q.execute());
  }

  /**
   * Count the number of documents which have a given name.
   * @param s active client session
   * @param db database name
   * @param path resource path
   * @return number of documents
   * @throws BaseXException query exception
   */
  static int countExact(final Session s, final String db, final String path)
      throws BaseXException {
    final String dbpath = db + SEP + path;
    final Query q1 = s.query("declare variable $d as xs:string external; "
        + "declare variable $p as xs:string external; "
        + "count(db:list($d)[. = $p])");
    q1.bind("$d", dbpath);
    q1.bind("$p", path);
    return parseInt(q1.execute());
  }

  /**
   * Check a folder for a dummy document and delete it.
   * @param s active client session
   * @param db database name
   * @param p path
   * @return {@code true} if dummy document existed
   * @throws BaseXException query exception
   */
  static boolean deleteDummy(final Session s, final String db, final String p)
      throws BaseXException {
    final String dummy = p + SEP + DUMMY;
    if(count(s, db, dummy) <= 0) return false;

    // path contains dummy document
    s.execute(new Open(db));
    s.execute(new Delete(dummy));
    return true;
  }

  /**
   * Check a folder for a dummy document and create it.
   * @param s active client session
   * @param db database name
   * @param p path
   * @return {@code true} if dummy document did not exist
   * @throws BaseXException query exception
   */
  static boolean createDummy(final Session s, final String db, final String p)
      throws BaseXException {
    final String dummy = p + SEP + DUMMY;
    if(count(s, db, dummy) > 0) return false;

    s.execute(new Open(db));
    s.execute(new Add(DUMMYCONTENT, DUMMY, p));
    return true;
  }
}
