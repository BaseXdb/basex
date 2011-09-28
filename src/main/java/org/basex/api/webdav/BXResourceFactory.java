package org.basex.api.webdav;

import static org.basex.api.webdav.BXResource.*;

import java.io.IOException;

import org.basex.api.HTTPSession;
import org.basex.server.LoginException;
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
  @Override
  public Resource getResource(final String host, final String dbpath) {
    final Auth a = HttpManager.request().getAuthorization();
    final String user = a != null ? a.getUser() : null;
    final String pass = a != null ? a.getPassword() : null;
    final HTTPSession session = new HTTPSession(user, pass);

    try {
      final Session s = session.login();
      try {
        // the root is requested
        final Path path = Path.path(dbpath).getStripFirst();
        if(path.isRoot()) return new BXRootResource(session);

        // only the database is requested
        final String db = path.getFirst();
        return path.getLength() == 1 ?
          listDBs(s).contains(db) ? new BXDatabase(db, session) : null :
          resource(s, db, path.getStripFirst().toString(), session);
      } finally {
        s.close();
      }
    } catch(final LoginException ex) {
      return BXNotAuthorizedResource.NOAUTH;
    } catch(final IOException ex) {
      Util.errln(ex);
    }
    return null;
  }
}
