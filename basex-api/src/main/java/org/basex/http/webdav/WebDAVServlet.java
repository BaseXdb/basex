package org.basex.http.webdav;

import jakarta.servlet.http.*;

import org.basex.http.restxq.*;

/**
 * This servlet processes WebDAV requests with the RESTXQ implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class WebDAVServlet extends RestXqServlet {
  @Override
  protected String path(final HttpServletRequest request) {
    // the annotations of the WebDAV modules include the servlet mapping
    final String info = request.getPathInfo();
    return request.getServletPath() + (info != null ? info : "");
  }
}
