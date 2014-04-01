package org.basex.http.webdav;

import static org.basex.http.webdav.BXServletRequest.*;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.http.webdav.impl.ResourceMetaData;
import org.basex.http.webdav.impl.ResourceMetaDataFactory;
import org.basex.http.webdav.impl.WebDAVService;
import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.common.*;
import com.bradmcevoy.http.*;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
public final class BXResourceFactory implements ResourceFactory,
    ResourceMetaDataFactory<BXAbstractResource> {

  /** HTTP Context. */
  private final HTTPContext http;
  /** WebDAV service. */
  private final WebDAVService<BXAbstractResource> service;

  /**
   * Constructor.
   * @param ht http context
   */
  BXResourceFactory(final HTTPContext ht) {
    http = ht;
    service = new WebDAVService<BXAbstractResource>(this, http);
  }

  /**
   * Closes the database session.
   * @throws LoginException login exception
   */
  void close() throws LoginException {
    service.session().close();
  }

  @Override
  public Resource getResource(final String host, final String dbpath) {
    final Auth a = HttpManager.request().getAuthorization();
    if(a != null) http.credentials(a.getUser(), a.getPassword());

    try {
      final HttpServletRequest r = getRequest();
      Path p = Path.path(dbpath);
      if(!r.getContextPath().isEmpty()) p = p.getStripFirst();
      if(!r.getServletPath().isEmpty()) p = p.getStripFirst();
      if(p.isRoot()) return new BXRoot(service);

      final String db = p.getFirst();
      return p.getLength() == 1 ?
        service.dbExists(db) ?
          new BXDatabase(new ResourceMetaData(db, service.timestamp(db)), service) :
          null :
        service.resource(db, p.getStripFirst().toString());
    } catch(final LoginException ex) {
      return BXNotAuthorizedResource.NOAUTH;
    } catch(final Exception ex) {
      Util.errln(ex);
    }
    return null;
  }

  @Override
  public BXFile file(final WebDAVService<BXAbstractResource> s, final ResourceMetaData d) {
    return new BXFile(d, s);
  }

  @Override
  public BXFolder folder(final WebDAVService<BXAbstractResource> s, final ResourceMetaData d) {
    return new BXFolder(d, s);
  }

  @Override
  public BXDatabase database(final WebDAVService<BXAbstractResource> s, final ResourceMetaData d) {
    return new BXDatabase(d, s);
  }
}
