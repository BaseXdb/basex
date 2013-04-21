package org.basex.http.webdav.milton2;

import io.milton.config.HttpManagerBuilder;
import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.ServletRequest;
import io.milton.servlet.ServletResponse;
import org.basex.http.BaseXServlet;
import org.basex.http.HTTPContext;
import org.basex.util.Util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * WebDAV servlet.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Dimitar Popov
 */
public final class WebDAVServlet extends BaseXServlet {
  /** Name of the milton2 enterprise implementation of {@link HttpManagerBuilder}. */
  private static final String HTTP_MANAGER_BUILDER_ENT =
      "io.milton.ent.config.HttpManagerBuilderEnt";
  /** Class implementing {@link HttpManagerBuilder}. */
  private Class<? extends HttpManagerBuilder> httpManagerBuilderClass;

  @Override
  @SuppressWarnings("unchecked")
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      httpManagerBuilderClass = (Class<? extends HttpManagerBuilder>) config.
          getServletContext().getClassLoader().loadClass(HTTP_MANAGER_BUILDER_ENT);
      Util.debug("Using milton2 server enterprise");
    } catch(ClassNotFoundException e) {
      Util.debug("milton2 server enterprise is not available");
    }
  }

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

  /**
   * Create a new {@link HttpManagerBuilder}.
   * @return new instance
   */
  private HttpManagerBuilder newHttpManagerBuilder() {
    try {
      if(httpManagerBuilderClass != null) return httpManagerBuilderClass.newInstance();
    } catch(Exception e) {
      Util.debug("Cannot create instance of milton2 enterprise HttpManagerBuilder");
    }
    return new HttpManagerBuilder();
  }
}
