package org.basex.http.webdav;

import static jakarta.servlet.http.HttpServletResponse.*;

import jakarta.servlet.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-24, BSD License
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
      conn.log(SC_OK, "");
    } finally {
      WebDAVFactory.close();
      response.close();
    }
  }
}
