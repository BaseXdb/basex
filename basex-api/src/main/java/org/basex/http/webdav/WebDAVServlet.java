package org.basex.http.webdav;

import java.io.*;

import org.basex.http.*;

import com.bradmcevoy.http.*;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {
  @Override
  protected void run(final HTTPContext http) throws IOException {
    final BXResourceFactory resources = new BXResourceFactory(http);
    final HttpManager manager = new HttpManager(resources);
    final Request request = new BXServletRequest(http.req);
    final Response response = new BXServletResponse(http.res);
    try {
      manager.process(request, response);
    } finally {
      resources.close();
      http.res.getOutputStream().flush();
      http.res.flushBuffer();
    }
  }
}
