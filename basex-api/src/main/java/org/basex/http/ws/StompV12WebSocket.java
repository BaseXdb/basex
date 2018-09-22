package org.basex.http.ws;

import java.util.*;
import java.util.function.*;

import javax.servlet.http.*;

import org.basex.http.web.*;
import org.basex.http.ws.stomp.*;
import org.basex.query.ann.*;
import org.basex.util.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines an abstract WebSocket. It inherits the Jetty WebSocket adapter.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
/*
 * [JF] Fragen:
 * - Wenn HeaderParam der vom XQuery-Nutzer gefordert wird nicht existiert wird fehler geworfen -> eig sollte default verwendet werden?
 *    -> Passiert auch unabhängig von STOMP!
 *    -> Hier relevant für header wie HOST, ...
 * */
public final class StompV12WebSocket extends WebSocket {

  /** Map for mapping stomids to the channels */
  private Map<String,String> StompIdChannel = new HashMap<>();
  /** List of all channels the WebSocket is connected to */
  private List<String> channels = new ArrayList<>();
  /**
   * Constructor.
   * @param req request
   * @param subprotocol subprotocol
   */
  StompV12WebSocket(final HttpServletRequest req, final String subprotocol) {
    super(req,subprotocol);
  }

  /**
   * Creates a new WebSocket instance.
   * @param req request
   * @param subprotocol subprotocol
   * @return WebSocket or {@code null}
   */
  static StompV12WebSocket get(final HttpServletRequest req, final String subprotocol) {
    final StompV12WebSocket ws = new StompV12WebSocket(req, subprotocol);
    try {
      if(!WebModules.get(ws.context).findWs(ws, null, ws.getPath()).isEmpty()) return ws;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
    return null;
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    super.onWebSocketConnect(sess);
  }

  @Override
  public void onWebSocketText(final String message) {
    StompFrame stompframe = parseStompFrame(message);
    if(stompframe == null) return;
    Map<String,String> stompheaders = stompframe.getHeaders();
    // Add the StompHeaders to the Headers
    final BiConsumer<String, String> addHeader = (k, v) -> {
      if(v != null) headers.put(k, v);
    };
    stompheaders.forEach(addHeader);

    switch(stompframe.getCommand()) {
      case CONNECT:
      case STOMP:
        Map<String, String> cHeader = new HashMap<>();
        cHeader.put("version", "1.1");
        ConnectedFrame cf = new ConnectedFrame(Commands.CONNECTED, cHeader, "");
        super.getSession().getRemote().sendStringByFuture(cf.serializedFrame());
        findAndProcess(Annotation._WS_STOMP_CONNECT, null, null);
        break;
      case SEND:
        String destination = stompheaders.get("destination");
        findAndProcess(Annotation._WS_MESSAGE, stompframe.getBody(), destination);
        break;
      case SUBSCRIBE:
        if(channels.contains(stompheaders.get("destination"))) {
          // Throw error? -> subscribe only one time to thechannel
          return;
        }
        channels.add(stompheaders.get("destination"));
        StompIdChannel.put(stompheaders.get("id"),stompheaders.get("destination"));
        WsPool.get().joinChannel(stompheaders.get("destination"), id);
        findAndProcess(Annotation._WS_STOMP_SUBSCRIBE, null, stompheaders.get("destination"));
        break;
      case UNSUBSCRIBE:
        String channel = StompIdChannel.get(stompheaders.get("id"));
        if(channel == null) return;
        WsPool.get().leaveChannel(channel, id);
        channels.remove(channel);
        StompIdChannel.remove(stompheaders.get("id"));
        findAndProcess(Annotation._WS_STOMP_UNSUBSCRIBE, null, channel);
        break;
      case ABORT:
      case ACK:
      case BEGIN:
      case COMMIT:
      case CONNECTED:
      case DISCONNECT:
      case ERROR:
      case MESSAGE:
      case NACK:
      case RECEIPT:
      default:
        findAndProcess(Annotation._WS_MESSAGE, message,null);
        break;
    };
  }


  /**
   * Parses a Stringmessage to a StompFrame.
   * @param message String
   * @return the StompFrame
   */
  private StompFrame parseStompFrame(final String message) {
    StompFrame stompframe = null;
    try {
      stompframe = StompFrame.parse(message);
    } catch(HeadersException e) {
      Util.debug(e);
      throw new CloseException(StatusCode.ABNORMAL, e.getMessage());
    }
    return stompframe;
  }

  /**
   * Finds a function and processes it.
   * @param ann annotation
   * @param message message (can be {@code null}; otherwise string or byte array)
   * @param path The WebSocketFunctionPath (can be {@code null}; if null, use path of the websocket)
   */
  private void findAndProcess(final Annotation ann, final Object message, String path) {
    String wspath = path == null ? this.getPath() : path;
    // check if an HTTP session exists, and if it still valid
    try {
      if(session != null) session.getCreationTime();
    } catch(final IllegalStateException ex) {
      session = null;
    }

    try {
      // find function to evaluate
      final WsFunction func = WebModules.get(context).websocket(this, ann, wspath);
      if(func != null) new WsResponse(this).create(func, message);
    } catch(final RuntimeException ex) {
      throw ex;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
  }
}
