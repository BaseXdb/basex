package org.basex.http.ws;

import static jakarta.servlet.http.HttpServletResponse.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.core.StaticOptions.*;
import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.web.*;
import org.basex.http.web.WebResponse.Response;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.websocket.*;
import jakarta.websocket.server.*;

/**
 * WebSocket servlet.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsServlet extends HttpServlet {
  /** Servlet-specific user. */
  private String username;
  /** Servlet-specific authentication method. */
  private AuthMethod auth;
  /** Maximum idle time in milliseconds. */
  private long idleTimeout;
  /** Maximum size of text messages ({@code -1}: container default). */
  private int maxText;
  /** Maximum size of binary messages ({@code -1}: container default). */
  private int maxBinary;

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
    // parse limits; they are assigned to each new connection
    idleTimeout = 1000L * number("maxIdleTime", 3600);
    maxText = number("maxTextMessageSize", -1);
    maxBinary = number("maxBinaryMessageSize", -1);
  }

  @Override
  protected void service(final HttpServletRequest request, final HttpServletResponse response)
      throws ServletException, IOException {

    // permission checks are matched against the client-visible path (including servlet mapping)
    final String pi = request.getPathInfo(), path = pi != null ? pi : "/";
    final HTTPConnection conn = new HTTPConnection(request, response, auth,
        request.getServletPath() + path);

    final WebSocket ws;
    try {
      conn.authenticate(username);

      // run permission checks if the path addresses a WebSocket function
      final WebModules modules = WebModules.get(conn.context);
      final WsFunction target = modules.websocket(path, conn.context);
      if(target != null) {
        // stop further processing if a check function produces a response
        for(final RestXqFunction check : modules.checks(conn)) {
          if(new RestXqResponse(conn).create(check, target, true) != Response.NONE) return;
        }
      }
      // create the WebSocket; refuse the upgrade if no function matches the path
      ws = WebSocket.get(request, conn.context.user(), idleTimeout, maxText, maxBinary);
      if(ws == null) throw HTTPStatus.SERVICE_NOT_FOUND.get();
    } catch(final Exception ex) {
      BaseXServlet.error(conn, ex);
      return;
    }

    // upgrade the connection: the container will request the WebSocket instance from the
    // configurator, after this request has been recycled
    final ServerContainer container =
      (ServerContainer) getServletContext().getAttribute(ServerContainer.class.getName());
    if(container == null) throw new ServletException("No WebSocket container available.");

    final ServerEndpointConfig config = ServerEndpointConfig.Builder.
      create(WebSocket.class, path).
      subprotocols(ws.subprotocol != null ? List.of(ws.subprotocol) : List.<String>of()).
      configurator(new WsConfigurator(ws)).
      build();
    try {
      container.upgradeHttpToWebSocket(request, response, config, Map.of());
    } catch(final DeploymentException ex) {
      throw new ServletException(ex);
    }
    conn.log(SC_SWITCHING_PROTOCOLS, "");
  }

  /**
   * Returns a numeric init parameter.
   * @param name name of parameter
   * @param dflt default value
   * @return value
   */
  private int number(final String name, final int dflt) {
    final String value = getInitParameter(name);
    return value != null ? Integer.parseInt(value) : dflt;
  }

  /** Configurator that supplies the WebSocket that was created during the handshake. */
  private static final class WsConfigurator extends ServerEndpointConfig.Configurator {
    /** WebSocket instance. */
    private final WebSocket ws;

    /**
     * Constructor.
     * @param ws WebSocket instance
     */
    private WsConfigurator(final WebSocket ws) {
      this.ws = ws;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getEndpointInstance(final Class<T> clazz) {
      return (T) ws;
    }
  }
}
