package org.basex.http.webdav;

import javax.servlet.http.*;

import org.basex.http.*;
import org.basex.server.*;
import org.basex.util.*;

import com.bradmcevoy.common.*;
import com.bradmcevoy.http.*;

/**
 * WebDAV resource factory. Main class for generating WebDAV resources.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Rositsa Shadura
 * @author Dimitar Popov
 */
final class WebDAVFactory implements ResourceFactory {
  /** Thread local variable to hold the current context. */
  private static final ThreadLocal<WebDAVService> SERVICES = new ThreadLocal<>();

  /**
   * Creates a new service.
   * @param http http context
   */
  static void init(final HTTPContext http) {
    SERVICES.set(new WebDAVService(http));
  }

  /**
   * Closes the database session.
   */
  static void close() {
    SERVICES.get().close();
    SERVICES.remove();
  }

  @Override
  public Resource getResource(final String host, final String dbpath) {
    try {
      final WebDAVService service = SERVICES.get();
      final HttpServletRequest r = service.http.req;
      Path p = Path.path(dbpath);
      if(!r.getContextPath().isEmpty()) p = p.getStripFirst();
      if(!r.getServletPath().isEmpty()) p = p.getStripFirst();
      if(p.isRoot()) return new WebDAVRoot(service);

      final String db = p.getFirst();
      return p.getLength() > 1 ?
        service.resource(db, p.getStripFirst().toString()) :
        service.dbExists(db) ?
          new WebDAVDatabase(new WebDAVMetaData(db, service.timestamp(db)), service) :
          null;
    } catch(final LoginException ex) {
      return WebDAVNotAuthorized.NOAUTH;
    } catch(final Exception ex) {
      Util.errln(ex);
    }
    return null;
  }

  /**
   * Creates a new resource representing a file.
   * @param s service instance
   * @param d file meta data
   * @return object representing the file
   */
  static WebDAVFile file(final WebDAVService s, final WebDAVMetaData d) {
    return new WebDAVFile(d, s);
  }

  /**
   * Creates a new resource representing a folder.
   * @param s service instance
   * @param d folder meta data
   * @return object representing the folder
   */
  static WebDAVFolder folder(final WebDAVService s, final WebDAVMetaData d) {
    return new WebDAVFolder(d, s);
  }

  /**
   * Creates a new resource representing a database.
   * @param s service instance
   * @param d database meta data
   * @return object representing the database
   */
  static WebDAVDatabase database(final WebDAVService s, final WebDAVMetaData d) {
    return new WebDAVDatabase(d, s);
  }
}
