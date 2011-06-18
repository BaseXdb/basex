package org.basex.api.webdav;

import static org.basex.api.webdav.WebDAVServer.*;
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
  /** Not authorized resource. */
  private static final Resource NOAUTH = new BXNotAuthorizedResource();

  @Override
  public Resource getResource(final String host, final String p) {
    final Auth a = HttpManager.request().getAuthorization();
    if(a == null && System.getProperty(DBUSER) == null) return NOAUTH;

    final Path path = Path.path(p);
    // root
    if(path.isRoot()) return new BXAllDatabasesResource();
    return null;
  }
}
