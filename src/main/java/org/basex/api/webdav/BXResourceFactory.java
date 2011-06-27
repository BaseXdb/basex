package org.basex.api.webdav;

import static org.basex.api.webdav.BXResource.*;
import static org.basex.api.webdav.BXNotAuthorizedResource.*;
import static org.basex.api.webdav.WebDAVServer.*;
import static org.basex.util.Token.*;
import java.io.IOException;

import org.basex.core.Context;
import org.basex.core.User;
import org.basex.server.ClientSession;
import org.basex.server.LocalSession;
import org.basex.server.Session;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** Stand-alone flag. */
  private final boolean standalone;
  /** Database context; will be null if {@link #standalone} is {@code false}. */
  private final Context ctx;

  /**
   * Constructor.
   * @param sa stand-alone flag
   */
  public BXResourceFactory(final boolean sa) {
    standalone = sa;
    ctx = standalone ? new Context() : null;
  }

  @Override
  public Resource getResource(final String host, final String dbpath) {
    final Auth a = HttpManager.request().getAuthorization();
    if(a == null && System.getProperty(DBUSER) == null) return NOAUTH;

    final Path p = Path.path(dbpath);
    // the root is requested
    if(p.isRoot()) return new BXAllDatabasesResource();

    final String db = p.getFirst();

    try {
      final Session s = login(a);
      try {
        // only the database is requested
        if(p.getLength() == 1)
          return listDatabases(s).contains(db) ? new BXDatabase(db) : null;
        return resource(s, db, stripLeadingSlash(p.getStripFirst().toString()));
      } finally {
        s.close();
      }
    } catch(Exception e) {
      // [DP] WebDAV: error handling
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Login to the database server.
   * @param a authentication
   * @return new session
   * @throws IOException I/O exception
   */
  Session login(final Auth a) throws IOException {
    return a == null ? login(null, null) : login(a.getUser(), a.getPassword());
  }

  /**
   * Login to the database server.
   * @param u user name
   * @param p user password
   * @return new session
   * @throws IOException I/O exception
   */
  Session login(final String u, final String p)
      throws IOException {
    final String host = System.getProperty(DBHOST);
    final int port = Integer.parseInt(System.getProperty(DBPORT));
    String user = System.getProperty(DBUSER);
    String pass = System.getProperty(DBPASS);
    if(user == null) {
      user = u;
      pass = p;
    }
    if(standalone) {
      // check if user exists
      final User usr = ctx.users.get(user);
      if(usr == null || !eq(usr.password, token(md5(pass)))) return null;
      return new LocalSession(ctx);
    }
    return new ClientSession(host, port, user, pass);
  }
}
