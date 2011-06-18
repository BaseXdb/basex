package org.basex.api.webdav;

import static java.lang.Integer.*;
import static org.basex.api.webdav.WebDAVServer.*;

import java.io.IOException;

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
  /** XML mime type. */
  static final String MIMETYPEXML = "text/xml";
  /** User name. */
  protected String user;
  /** User password. */
  protected String pass;
  /** Resource factory. */
  protected BXResourceFactory fact;

  @Override
  public Object authenticate(final String u, final String p) {
    this.user = u;
    this.pass = p;
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

  /**
   * List all databases.
   * @param cs session
   * @return a list of database names
   * @throws BaseXException query exception
   */
  static StringList listDatabases(final ClientSession cs)
      throws BaseXException {
    final StringList result = new StringList();
    final ClientQuery q = cs.query("db:list()");
    while(q.more())
      result.add(q.next());
    return result;
  }

  /**
   * Number of documents in the specified database.
   * @param cs session
   * @param db database name
   * @return number of documents
   * @throws BaseXException query exception
   */
  static int docNum(final ClientSession cs, final String db)
      throws BaseXException {
    return parseInt(cs.query("count(collection('" + db + "'))").execute());
  }

  /**
   * Get a valid database name from a general file name.
   * @param n name
   * @return valid database name
   */
  static String dbname(final String n) {
    final int i = n.lastIndexOf(".");
    return (i != -1 ? n.substring(0, i) : n).replaceAll("[^\\w-]", "");
  }

  /**
   * Login to the database server.
   * @param u user name
   * @param p user password
   * @return object representing the new client session
   * @throws IOException I/O exception
   */
  static ClientSession login(final String u, final String p)
      throws IOException {
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
}
