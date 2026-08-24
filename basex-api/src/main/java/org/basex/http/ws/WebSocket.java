package org.basex.http.ws;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.basex.core.*;
import org.basex.core.users.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.value.*;
import org.basex.server.*;
import org.basex.util.*;
import org.basex.util.http.*;
import org.basex.util.list.*;
import org.basex.util.log.*;

import jakarta.servlet.http.*;
import jakarta.websocket.*;

/**
 * This class defines a WebSocket. It implements the Jakarta WebSocket endpoint interface.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WebSocket extends Endpoint implements ClientInfo, WsSession {
  /** Marker for a ping frame in the send queue. */
  private static final Object PING = new Object();

  /** WebSocket attributes. */
  public final ConcurrentHashMap<String, Value> atts = new ConcurrentHashMap<>();
  /** Database context. */
  public final Context context;
  /** Concrete connection path. */
  public final String path;

  /** Request context (captured during the handshake). */
  final RequestContext requestCtx;

  /** Time when the connection was opened. */
  public final long created = System.currentTimeMillis();
  /** Time when the last message was received. */
  public volatile long accessed = created;

  /** Client WebSocket ID. */
  public String id;
  /** HTTP session (can be {@code null}; invalidated ones are dropped, maybe from a job thread). */
  public volatile HttpSession session;
  /** Negotiated sub-protocol ({@code null} if none). */
  public String subprotocol;

  /** Sub-protocols offered by the client. */
  private final String[] offered;
  /** Maximum idle time in milliseconds. */
  private final long idleTimeout;
  /** Maximum size of text messages ({@code -1}: container default). */
  private final int maxText;
  /** Maximum size of binary messages ({@code -1}: container default). */
  private final int maxBinary;
  /** Queued frames; also serves as monitor for the send state. */
  private final ArrayDeque<Object> queue = new ArrayDeque<>();

  /** Indicates that a frame is being sent (guarded by {@link #queue}). */
  private boolean sending;
  /** WebSocket session ({@code null} until the connection has been opened). */
  private volatile Session socket;

  /**
   * Constructor.
   * @param request request
   * @param user user that was authenticated during the handshake
   * @param idleTimeout maximum idle time in milliseconds
   * @param maxText maximum size of text messages ({@code -1}: container default)
   * @param maxBinary maximum size of binary messages ({@code -1}: container default)
   */
  private WebSocket(final HttpServletRequest request, final User user, final long idleTimeout,
      final int maxText, final int maxBinary) {
    final String pi = request.getPathInfo();
    path = pi != null ? pi : "/";
    final String sp = request.getHeader("Sec-WebSocket-Protocol");
    offered = sp != null ? sp.trim().split("\\s*,\\s*") : new String[0];
    session = request.getSession();
    this.idleTimeout = idleTimeout;
    this.maxText = maxText;
    this.maxBinary = maxBinary;
    // capture request values during the handshake, as the request is recycled afterwards
    requestCtx = new RequestContext(request).detach();
    context = new Context(HTTPContext.get().context(), this);
    context.user(user);
  }

  /**
   * Creates a new WebSocket instance.
   * @param request request
   * @param user user that was authenticated during the handshake
   * @param idleTimeout maximum idle time in milliseconds
   * @param maxText maximum size of text messages ({@code -1}: container default)
   * @param maxBinary maximum size of binary messages ({@code -1}: container default)
   * @return WebSocket, or {@code null} if no function matches the path
   * @throws QueryException query exception, raised if equally specific paths conflict
   * @throws IOException I/O exception
   */
  static WebSocket get(final HttpServletRequest request, final User user, final long idleTimeout,
      final int maxText, final int maxBinary) throws QueryException, IOException {
    final WebSocket ws = new WebSocket(request, user, idleTimeout, maxText, maxBinary);
    return WebModules.get(ws.context).websocket(ws) ? ws : null;
  }

  @Override
  public void onOpen(final Session sess, final EndpointConfig config) {
    socket = sess;
    sess.setMaxIdleTimeout(idleTimeout);
    if(maxText != -1) sess.setMaxTextMessageBufferSize(maxText);
    if(maxBinary != -1) sess.setMaxBinaryMessageBufferSize(maxBinary);
    sess.addMessageHandler(String.class, message -> {
      accessed = System.currentTimeMillis();
      findAndProcess(Annotation._WS_MESSAGE, message);
    });
    sess.addMessageHandler(ByteBuffer.class, buffer -> {
      accessed = System.currentTimeMillis();
      final byte[] payload = new byte[buffer.remaining()];
      buffer.get(payload);
      findAndProcess(Annotation._WS_MESSAGE, payload);
    });

    id = WsPool.add(this);
    run("[WS-OPEN] " + requestCtx.state().url(), null,
        () -> findAndProcess(Annotation._WS_CONNECT, null));
  }

  @Override
  public void onClose(final Session sess, final CloseReason reason) {
    final int status = reason.getCloseCode().getCode();
    final String message = reason.getReasonPhrase();
    try {
      run("[WS-CLOSE] " + requestCtx.state().url(), status,
          () -> findAndProcess(Annotation._WS_CLOSE,
              new WsFunction.CloseInfo(status, message != null ? message : "")));
    } finally {
      WsPool.remove(id);
    }
  }

  @Override
  public void onError(final Session sess, final Throwable th) {
    error(th);
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
   * Closes the WebSocket connection with the specified status.
   * @param status close status
   * @param reason close reason (can be {@code null})
   */
  public void close(final int status, final String reason) {
    // remove from the pool only after the close handler has run (via onClose)
    final Session sess = socket;
    if(sess != null && sess.isOpen()) {
      try {
        sess.close(new CloseReason(() -> status, reason));
      } catch(final IOException ex) {
        Util.debug(ex);
      }
    } else {
      WsPool.remove(id);
    }
  }

  /**
   * Sends a value to the connected client.
   * @param value byte buffer or string to be sent
   */
  public void send(final Object value) {
    enqueue(value);
  }

  /**
   * Sends a ping frame to the connected client.
   */
  public void ping() {
    enqueue(PING);
  }

  /**
   * Negotiates the sub-protocol: chooses the first protocol offered by the client
   * that has also been declared by the connect function.
   * @param declared declared sub-protocols
   */
  public void negotiate(final StringList declared) {
    for(final String offer : offered) {
      if(declared.contains(offer)) {
        subprotocol = offer;
        return;
      }
    }
  }

  /**
   * Queues a frame, and starts sending if no other frame is on its way. Only a single send
   * operation may be in progress for a session, so frames of all threads are serialized.
   * @param value frame to be queued
   */
  private void enqueue(final Object value) {
    final Session sess = socket;
    if(sess == null || !sess.isOpen()) return;
    synchronized(queue) {
      queue.add(value);
      if(sending) return;
      sending = true;
    }
    sendNext();
  }

  /**
   * Sends queued frames. Messages are sent asynchronously; the completion handler continues
   * with the next frame.
   */
  private void sendNext() {
    while(true) {
      final Object value;
      synchronized(queue) {
        value = queue.poll();
        if(value == null) {
          sending = false;
          return;
        }
      }
      try {
        final RemoteEndpoint.Async remote = socket.getAsyncRemote();
        if(value == PING) {
          // pings are sent synchronously, so the loop continues with the next frame
          remote.sendPing(ByteBuffer.allocate(0));
        } else if(value instanceof final ByteBuffer bb) {
          remote.sendBinary(bb, result -> sendNext());
          return;
        } else {
          remote.sendText((String) value, result -> sendNext());
          return;
        }
      } catch(final Exception ex) {
        // the connection is broken: discard all pending frames
        Util.debug(ex);
        synchronized(queue) {
          queue.clear();
          sending = false;
        }
        return;
      }
    }
  }

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string, byte array or close info)
   * @return {@code false} if the function raised an error
   */
  private boolean findAndProcess(final Annotation ann, final Object message) {
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
      return true;
    } catch(final Exception ex) {
      if(ann == Annotation._WS_ERROR) {
        context.log.write(LogType.ERROR, message(ex), null, context);
      }
      else error(ex);
      return false;
    }
  }

  /**
   * Reports an error to the error handler of the application.
   * @param th error
   */
  public void error(final Throwable th) {
    final String msg = message(th);
    run("[WS-ERROR] " + requestCtx.state().url() + ": " + msg, null,
        () -> findAndProcess(Annotation._WS_ERROR, msg));
  }

  /**
   * Returns the message of an error.
   * @param th error
   * @return error message
   */
  private static String message(final Throwable th) {
    Util.debug(th);
    final String msg = th.getMessage();
    return msg != null ? msg : Util.message(th);
  }

  /**
   * Runs a function and creates log output.
   * @param info log string
   * @param status close status (can be {@code null})
   * @param func function to be run
   */
  private void run(final String info, final Integer status, final BooleanSupplier func) {
    context.log.write(LogType.REQUEST, info, null, context);
    final Performance perf = new Performance();
    final boolean ok;
    try {
      ok = func.getAsBoolean();
    } catch(final Exception ex) {
      context.log.write(LogType.ERROR, "", perf, context);
      throw ex;
    }
    // a handler that raised an error has logged it; the request must not be reported as OK
    Object type = LogType.ERROR;
    if(ok) type = status != null ? status : LogType.OK;
    context.log.write(type, null, perf, context);
  }
}
