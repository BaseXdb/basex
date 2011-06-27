package org.basex.api.webdav;

import static java.lang.Integer.*;
import static org.basex.api.webdav.WebDAVServer.*;

import java.io.IOException;
import java.util.Date;

import org.basex.core.BaseXException;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.basex.util.StringList;

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
  static final char DIRSEP = '/';
  /** XML mime type. */
  static final String MIMETYPEXML = "text/xml";
  /** Zip mime type. */
  static final String MIMETYPEZIP = "application/zip";
  /** User name. */
  protected String user;
  /** User password. */
  protected String pass;
  /** Database. */
  protected final String db;
  /** Resource path (without leading '/'). */
  protected final String path;

  /**
   * Constructor.
   * @param d database name
   * @param pth resource path
   * @param u user name
   * @param p password
   */
  public BXResource(final String d, final String p) {
    db = d;
    path = stripLeadingSlash(p);
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
    final int idx = path.lastIndexOf(DIRSEP);
    return idx < 0 ? path : path.substring(idx + 1, path.length());
  }

  @Override
  public Date getModifiedDate() {
    return null;
  }

  /**
   * List all databases.
   * @param cs session
   * @return a list of database names
   * @throws BaseXException query exception
   */
  static StringList listDatabases(final ClientSession cs) throws BaseXException {
    final StringList result = new StringList();
    final ClientQuery q = cs.query("db:list()");
    while(q.more())
      result.add(q.next());
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
   * Login to the database server.
   * @param u user name
   * @param p user password
   * @return new client session
   * @throws IOException I/O exception
   */
  static ClientSession login(final String u, final String p) throws IOException {
    final String host = System.getProperty(DBHOST);
    final int port = Integer.parseInt(System.getProperty(DBPORT));
    String user = System.getProperty(DBUSER);
    String pass = System.getProperty(DBPASS);
    if(user == null) {
      user = u;
      pass = p;
    }
    return new ClientSession(host, port, user, pass);
  }

  /**
   * Login to the database server.
   * @param a authentication
   * @return new client session
   * @throws IOException I/O exception
   */
  static ClientSession login(final Auth a) throws IOException {
    return a == null ? login(null, null) : login(a.getUser(), a.getPassword());
  }

  /**
   * Creates a folder or document resource.
   * @param cs active client session
   * @param db database name
   * @param path resource path
   * @return requested resource or {@code null} if it does not exist
   * @throws BaseXException query exception
   */
  static Resource resource(final ClientSession cs, final String db,
      final String path) throws BaseXException {
    final String dbpath = db + DIRSEP + path;
    // check if there is a document in the collection having this path
    final ClientQuery d = cs.query("declare variable $d as xs:string external; "
        + "declare variable $p as xs:string external; "
        + "count(db:list($d)[. = $p])");
    d.bind("$d", dbpath);
    d.bind("$p", path);
    if(parseInt(d.execute()) == 1) return new BXDocument(db, path);

    // check if there are paths in the collection starting with this path
    final ClientQuery f = cs.query("declare variable $d as xs:string external; "
        + "declare variable $p as xs:string external; "
        + "count(db:list($d)[starts-with(., $p)])");
    f.bind("$d", dbpath);
    f.bind("$p", path);
    if(parseInt(f.execute()) > 0) return new BXFolder(db, path);
    return null;
  }

  /**
   * String leading slash if available.
   * @param s string to modify
   * @return string without leading slash
   */
  static String stripLeadingSlash(final String s) {
    return s != null && s.length() > 0 && s.charAt(0) == DIRSEP ? s.substring(1)
        : s;
  }

  /**
   * Check if content type is supported.
   * @param ctype content type
   * @return {@code true} if BaseX can handle the content type
   */
  static boolean supported(final String ctype) {
    // [DP] additional content types can be supported in the future
    return ctype != null
        && (ctype.indexOf(MIMETYPEXML) >= 0 || ctype.indexOf(MIMETYPEZIP) >= 0);
  }
}
