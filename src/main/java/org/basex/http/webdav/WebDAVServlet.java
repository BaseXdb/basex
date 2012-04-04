package org.basex.http.webdav;

import java.io.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws IOException {
    final HttpManager manager = new HttpManager(new BXResourceFactory(http));
    final Request request = new BXServletRequest(http.req);
    final Response response = new BXServletResponse(http.res);

    try {
      manager.process(request, response);
    } finally {
      http.res.getOutputStream().flush();
      http.res.flushBuffer();
    }
  }
}
