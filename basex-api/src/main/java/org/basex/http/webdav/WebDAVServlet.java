package org.basex.http.webdav;

import java.io.*;

import javax.servlet.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {
  /** Resource factory. */
  BXResourceFactory resources;
  /** Http Manager (must be a singleton). */
  HttpManager manager;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    resources = new BXResourceFactory();
    manager = new HttpManager(resources);
  }

  @Override
  protected void run(final HTTPContext http) throws IOException {
    // authorize request
    final BXServletRequest request = new BXServletRequest(http.req);
    final Auth a = request.getAuthorization();
    if(a != null) http.credentials(a.getUser(), a.getPassword());

    // initialize resource factory
    resources.init(http);

    // create response
    final BXServletResponse response = new BXServletResponse(http.res);
    try {
      manager.process(request, response);
    } finally {
      response.close();
      resources.close();
    }
  }
}
