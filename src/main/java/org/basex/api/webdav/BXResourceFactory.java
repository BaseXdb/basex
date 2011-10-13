package org.basex.api.webdav;

import static org.basex.api.webdav.BXNotAuthorizedResource.*;
import static org.basex.api.webdav.BXServletRequest.*;

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
        Path p = Path.path(dbpath);
        if(!getRequest().getContextPath().isEmpty()) p = p.getStripFirst();
        if(!getRequest().getServletPath().isEmpty()) p = p.getStripFirst();

        if(p.isRoot()) return new BXRoot(session);

        final String db = p.getFirst();
        return p.getLength() == 1 ?
          listDBs(s).contains(db) ? database(s, db, session) : null :
          resource(s, db, p.getStripFirst().toString(), session);
      } finally {
        s.close();
      }
    } catch(final LoginException ex) {
      return NOAUTH;
    } catch(final IOException ex) {
      Util.errln(ex);
    }
    return null;
  }
}
