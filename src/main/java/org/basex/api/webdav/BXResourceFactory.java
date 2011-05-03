package org.basex.api.webdav;

import org.basex.core.Context;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 * @author BaseX Team 2005-11, BSD License
 * @author Rositsa Shadura, Dimitar Popov
 */
public class BXResourceFactory implements ResourceFactory {
  /** Database context. */
  private Context ctx;

  /**
   * Constructor.
   * @param c database context
   */
  public BXResourceFactory(final Context c) {
    ctx = c;
  }

  @Override
  public Resource getResource(final String host, final String p) {
    final Path path = Path.path(p).getStripFirst();
    if(path.isRoot()) return new BXAllDatabasesResource(ctx);
    // TODO
    return null;
  }
}
