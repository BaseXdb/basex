package org.basex.api.webdav;

import static org.basex.api.webdav.BXResource.*;
import static org.basex.api.webdav.BXNotAuthorizedResource.*;
import static org.basex.api.webdav.WebDAVServer.*;
import org.basex.server.ClientSession;
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
  @Override
  public Resource getResource(final String host, final String dbpath) {
    final Auth a = HttpManager.request().getAuthorization();
    if(a == null && System.getProperty(DBUSER) == null) return NOAUTH;

    final Path p = Path.path(dbpath);
    // the root is requested
    if(p.isRoot())
      return new BXAllDatabasesResource(a.getUser(), a.getPassword());

    final String db = p.getFirst();

    try {
      final ClientSession s = login(a);
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
}
