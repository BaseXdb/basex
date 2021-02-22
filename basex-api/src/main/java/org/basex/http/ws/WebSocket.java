package org.basex.http.ws;

import static org.basex.http.HTTPText.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.ann.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.server.*;
import org.basex.server.Log.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines an abstract WebSocket. It inherits the Jetty WebSocket adapter.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Johannes Finckh
 */
public final class WebSocket extends WebSocketAdapter implements ClientInfo {
  /** WebSocket attributes. */
  public final ConcurrentHashMap<String, Value> atts = new ConcurrentHashMap<>();
  /** Database context. */
  public final Context context;
  /** Path. */
  public final WsPath path;

  /** Header parameters. */
  final Map<String, Value> headers = new HashMap<>();
  /** Servlet request. */
  final HttpServletRequest request;

  /** Client WebSocket id. */
  public String id;
  /** HTTP Session. */
  public HttpSession session;

  /**
   * Constructor.
   * @param request request
   */
  private WebSocket(final HttpServletRequest request) {
    this.request = request;

    final String pi = request.getPathInfo();
    path = new WsPath(pi != null ? pi : "/");
    session = request.getSession();

    final Context ctx = HTTPContext.get().context();
    context = new Context(ctx, this);
    context.user(ctx.user());
  }

  /**
   * Creates a new WebSocket instance.
   * @param request request
   * @return WebSocket or {@code null}
   */
  static WebSocket get(final HttpServletRequest request) {
    final WebSocket ws = new WebSocket(request);
    try {
      if(!WebModules.get(ws.context).findWs(ws, null).isEmpty()) return ws;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
    return null;
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    super.onWebSocketConnect(sess);
    id = WsPool.add(this);

    run("[WS-OPEN] " + request.getRequestURL(), null, () -> {
      // add headers (for binding them to the XQuery parameters in the corresponding bind method)
      final UpgradeRequest ur = sess.getUpgradeRequest();
      final BiConsumer<String, String> addHeader = (k, v) -> {
        if(v != null) headers.put(k, new Atm(v));
      };

      addHeader.accept("http-version", ur.getHttpVersion());
      addHeader.accept("origin", ur.getOrigin());
      addHeader.accept("protocol-version", ur.getProtocolVersion());
      addHeader.accept("query-string", ur.getQueryString());
      addHeader.accept("is-secure", String.valueOf(ur.isSecure()));
      addHeader.accept("request-uri", ur.getRequestURI().toString());
      addHeader.accept("host", ur.getHost());
      final TokenList protocols = new TokenList();
      for(final String protocol : ur.getSubProtocols()) protocols.add(protocol);
      headers.put("sub-protocols", StrSeq.get(protocols));

      findAndProcess(Annotation._WS_CONNECT, null);
    });
  }

  @Override
  public void onWebSocketError(final Throwable cause) {
    run("[WS-ERROR] " + request.getRequestURL() + ": " + cause.getMessage(), null,
        () -> findAndProcess(Annotation._WS_ERROR, cause.getMessage()));
  }

  @Override
  public void onWebSocketClose(final int status, final String message) {
    try {
      run("[WS-CLOSE] " + request.getRequestURL(), status,
          () -> findAndProcess(Annotation._WS_CLOSE, null));
    } finally {
      WsPool.remove(id);
      super.onWebSocketClose(status, message);
    }
  }

  @Override
  public void onWebSocketText(final String message) {
    findAndProcess(Annotation._WS_MESSAGE, message);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    findAndProcess(Annotation._WS_MESSAGE, payload);
  }

  @Override
  public String clientAddress() {
    return HTTPConnection.remoteAddress(request);
  }

  @Override
  public String clientName() {
    Object value = atts.get(CLIENT_ID);
    if(value == null && session != null) value = session.getAttribute(CLIENT_ID);
    return clientName(value, context);
  }

  /**
   * Closes the WebSocket connection.
   */
  public void close() {
    WsPool.remove(id);
    getSession().close();
  }

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string or byte array)
   */
  private void findAndProcess(final Annotation ann, final Object message) {
    // check if an HTTP session exists, and if it still valid
    try {
      if(session != null) session.getCreationTime();
    } catch(final IllegalStateException ex) {
      session = null;
    }

    try {
      // find function to evaluate
      final WsFunction func = WebModules.get(context).websocket(this, ann);
      if(func != null) new WsResponse(this).create(func, message, true);
    } catch(final Exception ex) {
      error(ex);
    }
  }

  /**
   * Sends an error to the client.
   * @param ex exception
   */
  public void error(final Exception ex) {
    Util.debug(ex);
    try {
      getRemote().sendString(ex.getMessage());
    } catch(final IOException e) {
      Util.debug(e);
    }
  }

  /**
   * Runs a function and creates log output.
   * @param info log string
   * @param status close status
   * @param func function to be run
   */
  private void run(final String info, final Integer status, final Runnable func) {
    context.log.write(LogType.REQUEST, info, null, context);
    final Performance perf = new Performance();
    try {
      func.run();
    } catch (final Exception ex) {
      context.log.write(LogType.ERROR, "", perf, context);
      throw ex;
    }
    context.log.write(status != null ? status : LogType.OK, null, perf, context);
  }
}
