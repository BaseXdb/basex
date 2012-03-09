package org.basex.api.webdav;

import java.io.*;

import javax.servlet.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public class WebDAVServlet extends HttpServlet {
  @Override
  public void service(final HttpServletRequest req, final HttpServletResponse res)
      throws IOException {

    final HttpManager manager = new HttpManager(new BXResourceFactory());
    final Request request = new BXServletRequest(req);
    final Response response = new BXServletResponse(res);
    try {
      manager.process(request, response);
    } finally {
      res.getOutputStream().flush();
      res.flushBuffer();
    }
  }
}
