package org.basex.api.webdav;

import java.io.IOException;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.basex.core.Text;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Response;

/**
 * BaseX WebDAV servlet.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public class WebDAVServlet implements Servlet {
  /** Milton resource manager. */
  private HttpManager manager = new HttpManager(new BXResourceFactory());

  @Override
  public void init(final ServletConfig config) throws ServletException { }

  @Override
  public ServletConfig getServletConfig() {
    return null;
  }

  @Override
  public void service(final ServletRequest req, final ServletResponse res)
      throws ServletException, IOException {
    final Request request = new BXServletRequest((HttpServletRequest) req);
    final Response response = new BXServletResponse((HttpServletResponse) res);
    try {
      manager.process(request, response);
    } finally {
      res.getOutputStream().flush();
      res.flushBuffer();
    }
  }

  @Override
  public String getServletInfo() {
    return Text.NAME + "Servlet";
  }

  @Override
  public void destroy() { }
}
