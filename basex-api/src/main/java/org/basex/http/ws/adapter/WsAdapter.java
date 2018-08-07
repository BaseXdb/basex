package org.basex.http.ws.adapter;

import static org.basex.http.web.WebText.*;

import java.util.*;
import java.util.function.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.web.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;
import org.basex.server.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines an abstract WebSocket adapter. It inherits the Jetty WebSocket adapter.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public abstract class WsAdapter extends WebSocketAdapter implements ClientInfo {
  /** Servlet request. */
  protected final HttpServletRequest req;
  /** Path. */
  protected final WsPath path;

  /** Database context. */
  public final Context context;
  /** Session for the connection; will be assigned when the connection is built up. */
  public Session session;
  /** HTTP Session. */
  public HttpSession httpsession;
  /** Client id. */
  public String id;
  /** Response serializer. */
  public WsResponse response;
  /** Header parameters. */
  public final Map<String, String> headers = new HashMap<>();

  /**
   * Constructor.
   * @param req request
   */
  protected WsAdapter(final HttpServletRequest req) {
    this.req = req;
    final String pi = req.getPathInfo();
    this.path = new WsPath(pi != null ? pi : "/");
    response = new StandardWsResponse();
    httpsession = req.getSession();

    final Context ctx = HTTPContext.context();
    context = new Context(ctx, this);
    context.user(ctx.user());
  }

  /**
   * Returns the client path.
   * @return path
   */
  public String getPath() {
    return path.toString();
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    context.log.write(WS_ADAPTER_CONNECT, sess.toString(), null, context);
    super.onWebSocketConnect(sess);

    final UpgradeRequest ur = sess.getUpgradeRequest();
    final WsPool pool = WsPool.get();
    try {
      if(path != null) {
        id = pool.joinChannel(this, path.toString());
      } else {
        id = pool.join(this);
      }
    } catch (IllegalStateException ex) {
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }

    final BiConsumer<String, String> addHeader = (k, v) -> {
      if(v != null) headers.put(k, v);
    };

    // add headers (for binding them to the XQuery parameters in the corresponding bind method)
    addHeader.accept("Http-Version", ur.getHttpVersion());
    addHeader.accept("Origin", ur.getOrigin());
    addHeader.accept("Protocol-version", ur.getProtocolVersion());
    addHeader.accept("QueryString", ur.getQueryString());
    addHeader.accept("IsSecure", String.valueOf(ur.isSecure()));
    addHeader.accept("RequestURI", ur.getRequestURI().toString());

    final String[] names = { "Host", "Sec-WebSocket-Version" };
    for(final String name : names) addHeader.accept(name, ur.getHeader(name));

    session = sess;
    findAndProcess(Annotation._WS_CONNECT, null);
  }

  /*
   * This is a way for the internal implementation to notify of exceptions that occurred during the
   * WebSocket processing. Usually this occurs from bad / malformed incoming packets. (example:
   * bad UTF8 data, frames that are too big, violations of the spec). This will result in the
   * Session being closed by the implementing side.
   */
  @Override
  public void onWebSocketError(final Throwable cause) {
    context.log.write(WS_ADAPTER_ERROR, cause.getMessage(), null, context);
    findAndProcess(Annotation._WS_ERROR, cause.toString());
    super.getSession().close();
    removeWebSocket();
  }

  @Override
  public void onWebSocketClose(final int statusCode, final String reason) {
    context.log.write(WS_ADAPTER_CLOSE, reason, null, context);
    findAndProcess(Annotation._WS_CLOSE, null);
    super.onWebSocketClose(statusCode, reason);
    removeWebSocket();
  }

  /**
   * Removes the WebSocket from the pool. Removes it from all channels in the pool as well.
   */
  protected abstract void removeWebSocket();

  @Override
  public String clientAddress() {
    return session != null ? session.getRemoteAddress().toString() : null;
  }

  @Override
  public String clientName() {
    return context.user().name();
  }

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string or byte array)
   */
  protected void findAndProcess(final Annotation ann, final Object message) {
    try {
      if(httpsession != null) httpsession.getCreationTime();
    } catch (IllegalStateException ex) {
      httpsession = null;
    }

    try {
      final WebModules modules = WebModules.get(context);
      final WsFunction func = modules.find(this, ann);
      if(func == null) throw new Exception(Util.info(WS_MISSING_X, ann));
      else if(response != null) func.process(this, message);
    } catch(final RuntimeException ex) {
      throw ex;
    } catch(final Exception ex) {
      Util.debug(ex);
      // [JF] Alternative for raising error by ourselves: throw new RuntimeException(ex);
      // Close exception with status code abnormal (not send/recieve by WebsocketClose)
      // --> gets logged in console too!
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
  }
}
