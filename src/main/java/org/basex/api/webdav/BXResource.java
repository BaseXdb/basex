package org.basex.api.webdav;

import java.util.Date;
import org.basex.core.BaseXException;
import org.basex.server.Query;
import org.basex.server.Session;
import org.basex.util.Util;
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
  static final String EMPTYXML = "EMPTY.xml";
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
   * Prints exception message to standard error.
   * @param ex exception
   */
  static void handle(final Exception ex) {
    Util.errln(ex.getMessage());
  }
}
