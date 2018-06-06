package org.basex.http.webdav;

import javax.servlet.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {
  /** Http Manager (must be a singleton). */
  private HttpManager manager;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    manager = new HttpManager(new WebDAVFactory());
  }

  @Override
  protected void run(final HTTPConnection conn) {
    // initialize resource factory
    WebDAVFactory.init(conn);

    // create response
    final WebDAVRequest request = new WebDAVRequest(conn);
    final WebDAVResponse response = new WebDAVResponse(conn);
    try {
      manager.process(request, response);
    } finally {
      WebDAVFactory.close();
      response.close();
    }
  }
}
