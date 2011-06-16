package org.basex.api.webdav;

import static org.basex.api.webdav.WebDAVServer.*;

import java.io.IOException;

import org.basex.core.BaseXException;
import org.basex.server.ClientQuery;
import org.basex.server.ClientSession;
import org.basex.util.StringList;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** XML mime type. */
  static final String MIMETYPEXML = "text/xml";

  @Override
  public Resource getResource(final String host, final String p) {
    final Path path = Path.path(p);
    // root
    if(path.isRoot()) return new BXAllDatabasesResource();
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
    while(q.more()) result.add(q.next());
    return result;
  }

  /**
   * Is the specified database a collection of documents?
   * @param cs session
   * @param db database name
   * @return <code>true</code> if the database has more than one document
   * @throws BaseXException query exception
   */
  static boolean isCollection(final ClientSession cs, final String db)
      throws BaseXException {
    return "0".equals(cs.query("count(collection('" + db + "'))").execute());
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
   * @param auth user authentication
   * @return object representing the new client session
   * @throws IOException I/O exception
   */
  static ClientSession login(final Auth auth) throws IOException {
    final String host = System.getProperty(DBHOST);
    final int port = Integer.parseInt(System.getProperty(DBPORT));
    String user = System.getProperty(DBUSER);
    String pass = System.getProperty(DBPASS);
    if(user == null) {
      if(auth == null) return null;
      user = auth.getUser();
      pass = auth.getPassword();
    }

    return new ClientSession(host, port, user, pass);
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
