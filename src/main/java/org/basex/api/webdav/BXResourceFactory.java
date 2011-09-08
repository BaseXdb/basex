package org.basex.api.webdav;

import static org.basex.api.webdav.BXNotAuthorizedResource.*;

import org.basex.api.HTTPContext;
import org.basex.api.HTTPSession;
import org.basex.server.Session;
import org.basex.util.Util;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** HTTP context. */
  private final HTTPContext http;

  /**
   * Constructor.
   * @param ht HTTP context
   */
  public BXResourceFactory(final HTTPContext ht) {
    http = ht;
  }

  @Override
  public Resource getResource(final String host, final String dbpath) {
    final Auth a = HttpManager.request().getAuthorization();
    final String user = a != null ? a.getUser() : null;
    final String pass = a != null ? a.getPassword() : null;
    final HTTPSession session = http.session(user, pass);
    if(!session.valid()) return NOAUTH;

    final Path path = Path.path(dbpath);
    // the root is requested
    if(path.isRoot()) return new BXRootResource(session);

    final String db = path.getFirst();
    try {
      final Session s = session.login();
      try {
        // only the database is requested
        return path.getLength() == 1 ?
          listDBs(s).contains(db) ? new BXDatabase(db, session) : null :
          resource(s, db, path.getStripFirst().toString(), session);
      } finally {
        s.close();
      }
    } catch(final Exception ex) {
      Util.errln(ex);
    }
    return null;
  }
}
