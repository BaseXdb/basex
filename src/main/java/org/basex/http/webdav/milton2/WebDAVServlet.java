package org.basex.http.webdav.milton2;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.ServletRequest;
import io.milton.servlet.ServletResponse;
import org.basex.http.BaseXServlet;
import org.basex.http.HTTPContext;
import java.io.IOException;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {

  @Override
  protected void run(final HTTPContext http) throws IOException {
    final HttpManagerBuilder b = newHttpManagerBuilder();
    b.setEnabledJson(false);
    b.setWebdavEnabled(true);
    b.setResourceFactory(new BXResourceFactory(http));
    b.init();

    final HttpManager manager = b.buildHttpManager();
    final Request request = new ServletRequest(http.req, getServletContext());
    final Response response = new ServletResponse(http.res);

    try {
      manager.process(request, response);
    } finally {
      http.res.getOutputStream().flush();
      http.res.flushBuffer();
    }
  }

  private static HttpManagerBuilder newHttpManagerBuilder() {
    // [DP] check if io.milton.ent.config.HttpManagerBuilderEnt is available
    return new HttpManagerBuilder();
  }
}
