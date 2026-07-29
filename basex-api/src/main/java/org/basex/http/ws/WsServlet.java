package org.basex.http.ws;

import java.io.*;
import java.time.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.web.*;
import org.basex.http.web.WebResponse.Response;
import org.basex.util.http.*;
import org.eclipse.jetty.ee10.websocket.server.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

/**
 * WebSocket servlet.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsServlet extends JettyWebSocketServlet {
  /** Servlet-specific user. */
  private String username;
  /** Servlet-specific authentication method. */
  private AuthMethod auth;

  @Override
  public void configure(final JettyWebSocketServletFactory factory) {
    final Map<String, ?> map = HTTPContext.get().initParams();
    final Object it = map.get("maxIdleTime");
    factory.setIdleTimeout(Duration.ofSeconds(it != null ? Long.parseLong(it.toString()) : 3600));
    final Object mtms = map.get("maxTextMessageSize");
    if(mtms != null) factory.setMaxTextMessageSize(Long.parseLong(mtms.toString()));
    final Object mbms = map.get("maxBinaryMessageSize");
    if(mbms != null) factory.setMaxBinaryMessageSize(Long.parseLong(mbms.toString()));

    factory.setCreator(new WsCreator());
  }

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    try {
      HTTPContext.get().init(config.getServletContext());
    } catch(final IOException ex) {
      throw new ServletException(ex);
    }
    // parse servlet-specific user and authentication method
    username = BaseXServlet.initParam(config, StaticOptions.USER.name());
    final String method = BaseXServlet.initParam(config, StaticOptions.AUTHMETHOD.name());
    if(method != null) auth = AuthMethod.valueOf(method);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    // permission checks are matched against the client-visible path (including servlet mapping)
    final String pi = request.getPathInfo(), path = pi != null ? pi : "/";
    final HTTPConnection conn = new HTTPConnection(request, response, auth,
        request.getServletPath() + path);
    try {
      conn.authenticate(username);
      // pass authenticated user on to the WebSocket instance
      request.setAttribute(HTTPText.REQUEST_USER, conn.context.user());

      // run permission checks if the path addresses a WebSocket function
      final WebModules modules = WebModules.get(conn.context);
      final WsFunction target = modules.websocket(path, conn.context);
      if(target != null) {
        // stop further processing if a check function produces a response
        for(final RestXqFunction check : modules.checks(conn)) {
          if(new RestXqResponse(conn).create(check, target, true) != Response.NONE) return;
        }
      }
    } catch(final Exception ex) {
      BaseXServlet.error(conn, ex);
      return;
    }
    // upgrade the connection
    super.service(request, response);
    conn.log(response.getStatus(), "");
  }
}
