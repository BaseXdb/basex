package org.basex.http.webdav.milton2;

import io.milton.config.*;
import io.milton.http.*;
import io.milton.servlet.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.basex.http.*;
import org.basex.util.*;

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
  private static Class<? extends HttpManagerBuilder> httpManagerBuilderClass = 
      findHttpManagerBuilderClass();

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
  
  /**
   * Search the class path for the enterprise version of Milton2.
   * @return {@code null} if the enterprise version of Milton2 is not available
   */
  @SuppressWarnings("unchecked")
  private static Class<? extends HttpManagerBuilder> findHttpManagerBuilderClass() {
    try {
      // use only for development; a valid license should be obtained for production!
      enableLocking();
      return (Class<? extends HttpManagerBuilder>)
          WebDAVServlet.class.getClassLoader().loadClass(HTTP_MANAGER_BUILDER_ENT);
    } catch(Throwable e) {
      Util.debug("milton2 server enterprise is not available");
      return null;
    }
  }

  /**
   * Circumvents the Milton2 checks for a valid license.<br/>
   * <b>Use only for development and testing!!!</b>
   * @throws Throwable error
   */
  private static void enableLocking() throws Throwable {
    Class<?> clz = Reflect.forName("io.milton.webdav.utils.LockUtils");
    Field field = clz.getDeclaredField("validatedLicenseProps");
    field.setAccessible(true);
    field.set(null, new Properties());
  }
}
