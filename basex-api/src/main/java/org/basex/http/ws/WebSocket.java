package org.basex.http.ws;

import java.nio.*;
import java.util.concurrent.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.ann.*;
import org.basex.query.value.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.log.*;
import org.eclipse.jetty.websocket.api.*;
import org.eclipse.jetty.websocket.api.exceptions.*;

import jakarta.servlet.http.*;

/**
 * This class defines a WebSocket. It implements the Jetty WebSocket session listener.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WebSocket extends Session.Listener.AbstractAutoDemanding
    implements ClientInfo, WsSession {
  /** WebSocket attributes. */
  public final ConcurrentHashMap<String, Value> atts = new ConcurrentHashMap<>();
  /** Database context. */
  public final Context context;
  /** Path. */
  public final WsPath path;

  /** Request context (captured during the handshake). */
  final RequestContext requestCtx;

  /** Client WebSocket ID. */
  public String id;
  /** HTTP Session. */
  public HttpSession session;

  /**
   * Constructor.
   * @param request request
   */
  private WebSocket(final HttpServletRequest request) {
    final String pi = request.getPathInfo();
    path = new WsPath(pi != null ? pi : "/");
    session = request.getSession();
    // capture request values during the handshake, as the request is recycled afterwards
    requestCtx = new RequestContext(request).detach();
    context = new Context(HTTPContext.get().context(), this);
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
  public void onWebSocketOpen(final Session sess) {
    super.onWebSocketOpen(sess);
    id = WsPool.add(this);
    run("[WS-OPEN] " + requestCtx.state().url(), null,
        () -> findAndProcess(Annotation._WS_CONNECT, null));
  }

  @Override
  public void onWebSocketError(final Throwable th) {
    final String m1 = th.getMessage(), m2 = Util.message(th), msg = m1 != null ? m1 : m2;
    run("[WS-ERROR] " + requestCtx.state().url() + ": " + msg, null,
        () -> findAndProcess(Annotation._WS_ERROR, msg));
  }

  @Override
  public void onWebSocketClose(final int status, final String message, final Callback callback) {
    try {
      run("[WS-CLOSE] " + requestCtx.state().url(), status,
          () -> findAndProcess(Annotation._WS_CLOSE, null));
    } finally {
      WsPool.remove(id);
      callback.succeed();
    }
  }

  @Override
  public void onWebSocketText(final String message) {
    findAndProcess(Annotation._WS_MESSAGE, message);
  }

  @Override
  public void onWebSocketBinary(final ByteBuffer buffer, final Callback callback) {
    try {
      final byte[] payload = new byte[buffer.remaining()];
      buffer.get(payload);
      findAndProcess(Annotation._WS_MESSAGE, payload);
      callback.succeed();
    } catch(final RuntimeException ex) {
      callback.fail(ex);
    }
  }

  @Override
  public String clientAddress() {
    return requestCtx.state().originalAddress();
  }

  @Override
  public String clientName() {
    final Object value = atts.get(HTTPText.CLIENT_ID);
    return clientName(value != null ? value :
      RequestState.attribute(session, HTTPText.CLIENT_ID), context);
  }

  @Override
  public HttpSession session() {
    return session;
  }

  /**
   * Closes the WebSocket connection.
   */
  public void close() {
    WsPool.remove(id);
    if(isOpen()) getSession().close();
  }

  /**
   * Sends a value to the connected client.
   * @param value byte buffer or string to be sent
   */
  public void send(final Object value) {
    final Session sess = getSession();
    if(sess == null || !sess.isOpen()) return;
    if(value instanceof final ByteBuffer bb) {
      sess.sendBinary(bb, Callback.NOOP);
    } else {
      sess.sendText((String) value, Callback.NOOP);
    }
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
      Util.debug(ex);
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
    send(ex.getMessage());
  }

  /**
   * Runs a function and creates log output.
   * @param info log string
   * @param status close status (can be {@code null})
   * @param func function to be run
   */
  private void run(final String info, final Integer status, final Runnable func) {
    context.log.write(LogType.REQUEST, info, null, context);
    final Performance perf = new Performance();
    try {
      func.run();
    } catch(final Exception ex) {
      context.log.write(LogType.ERROR, "", perf, context);
      throw ex;
    }
    context.log.write(status != null ? status : LogType.OK, null, perf, context);
  }
}
