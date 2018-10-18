package org.basex.http.ws;

import java.util.*;
import java.util.function.*;

import javax.servlet.http.*;

import org.basex.http.web.*;
import org.basex.http.ws.stomp.*;
import org.basex.http.ws.stomp.frames.*;
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
 * [JF] Fragen: - Wenn HeaderParam der vom XQuery-Nutzer gefordert wird nicht existiert wird fehler
 * geworfen -> eig sollte default verwendet werden? -> Passiert auch unabhängig von STOMP! -> Hier
 * relevant für header wie HOST, ...
 */
public final class StompV12WebSocket extends WebSocket {

  /** Map for mapping stomids to the channels */
  private Map<String, String> StompIdChannel = new HashMap<>();
  /** List of all channels the WebSocket is connected to */
  private List<String> channels = new ArrayList<>();
  /** Map of stompids with ackmode */
  private Map<String, String> stompAck = new HashMap<>();
  /** Map of Transactionids with a List of StompFrames */
  private Map<String, List<StompFrame>> transidStompframe = new HashMap<>();

  /**
   * Consumer for adding headers.
   */
  final BiConsumer<String, String> addHeader = (k, v) -> {
    if(v != null) headers.put(k, v);
  };

  /**
   * Constructor.
   * @param req request
   * @param subprotocol subprotocol
   */
  StompV12WebSocket(final HttpServletRequest req, final String subprotocol) {
    super(req, subprotocol);
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

  /**
   * Returns the Ack-Mode for the StompId.
   * @param stompId StompId
   * @return String ACK-Mode
   */
  public String getAckMode(final String stompId) {
    return stompAck.get(stompId);
  }

  /**
   * Returns the StompId to a Channel.
   * @param channel Channel
   * @return String the stompId, can be {@code null};
   */
  public String getStompId(final String channel) {
    for(String stompid : StompIdChannel.keySet()) {
      if(StompIdChannel.get(stompid).equals(channel)) return stompid;
    }
    return null;
  }

  /**
   * Executes all StompFrames in the Transaction
   * @param transactionId The id of the Transaction
   */
  private void commitTransaction(final String transactionId) {
    List<StompFrame> frames = transidStompframe.get(transactionId);
    if(frames == null) return;
    for(StompFrame stompframe : frames) {
      Map<String, String> stompheaders = stompframe.getHeaders();
      stompheaders.forEach(addHeader);
      switch(stompframe.getCommand()) {
        case SEND:
          findAndProcess(Annotation._WS_STOMP_MESSAGE, stompframe.getBody(),
              stompheaders.get("destination"));
          break;
        case ACK:
          String ackMode = getAckMode(WsPool.get().getStompIdToMessageId(stompheaders.get("id")));
          if(ackMode.equals("client")) {
            WsPool.get().ackMessages(id, stompheaders.get("id"));
          } else if(ackMode.equals("client-individual")) {
            WsPool.get().ackMessage(id, stompheaders.get("id"));
          }
          break;
        case NACK:
          MessageObject mo = WsPool.get().discardMessage(id, stompheaders.get("id"));
          if(mo == null) return;
          addHeader.accept("messageid", mo.getMessageId());
          addHeader.accept("message", mo.getMessage());
          addHeader.accept("wsid", mo.getWebSocketId());
          findAndProcess(Annotation._WS_STOMP_NACK, null, null);
          break;
        default:
          break;
      }
      for(String headername : stompheaders.keySet()) {
        headers.remove(headername);
      }
      headers.remove("messageid");
      headers.remove("message");
      headers.remove("wsid");
    }
  }

  @Override
  public void onWebSocketConnect(final Session sess) {
    super.onWebSocketConnect(sess);
  }

  /**
   * Sends an Errorframe
   * @param message error message
   */
  private void sendError(final String message) {
    Map<String, String> cheaders = new HashMap<>();
    cheaders.put("message", message);
    ErrorFrame ef = new ErrorFrame(Commands.ERROR, cheaders, "");
    super.getSession().getRemote().sendStringByFuture(ef.serializedFrame());
  }

  @Override
  public void onWebSocketText(final String message) {
    StompFrame stompframe = parseStompFrame(message);
    if(stompframe == null) return;
    Map<String, String> stompheaders = stompframe.getHeaders();
    // Add the StompHeaders to the Headers
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
        if(stompheaders.get("transaction") != null) {
          // If the Message is in an transaction, dont execute it, add it to the transaction.
          // Execute after Commit
          if(!transidStompframe.containsKey(stompheaders.get("transaction"))) {
            sendError("No Transaction found");
            return;
          }
          List<StompFrame> sf = transidStompframe.get(stompheaders.get("transaction"));
          if(sf == null) {
            sf = new ArrayList<>();
            transidStompframe.put(stompheaders.get("transaction"), sf);
          }
          sf.add(stompframe);

        } else {
          String destination = stompheaders.get("destination");
          findAndProcess(Annotation._WS_STOMP_MESSAGE, stompframe.getBody(), destination);
        }
        break;
      case SUBSCRIBE:
        if(channels.contains(stompheaders.get("destination"))) {
          sendError("No Destination found");
          return;
        }
        channels.add(stompheaders.get("destination"));
        StompIdChannel.put(stompheaders.get("id"), stompheaders.get("destination"));
        stompAck.put(stompheaders.get("id"), stompheaders.get("ack"));
        WsPool.get().joinChannel(stompheaders.get("destination"), id);
        findAndProcess(Annotation._WS_STOMP_SUBSCRIBE, null, stompheaders.get("destination"));
        break;
      case UNSUBSCRIBE:
        String channel = StompIdChannel.get(stompheaders.get("id"));
        if(channel == null) return;
        WsPool.get().leaveChannel(channel, id);
        channels.remove(channel);
        StompIdChannel.remove(stompheaders.get("id"));
        stompAck.remove(stompheaders.get("id"));
        findAndProcess(Annotation._WS_STOMP_UNSUBSCRIBE, null, channel);
        break;
      case ACK:
        if(stompheaders.get("transaction") != null) {
          // If the ACK is in an transaction, dont execute it, add it to the transaction.
          // Execute after Commit
          if(!transidStompframe.containsKey(stompheaders.get("transaction"))) {
            sendError("No Transaction found");
            return;
          }
          List<StompFrame> sf = transidStompframe.get(stompheaders.get("transaction"));
          if(sf == null) sf = new ArrayList<>();
          sf.add(stompframe);
        } else {
          // get the ackmode of the subscribtion
          String ackMode = getAckMode(WsPool.get().getStompIdToMessageId(stompheaders.get("id")));
          if(ackMode.equals("client")) {
            WsPool.get().ackMessages(id, stompheaders.get("id"));
          } else if(ackMode.equals("client-individual")) {
            WsPool.get().ackMessage(id, stompheaders.get("id"));
          }
        }
        break;
      case NACK:
        if(stompheaders.get("transaction") != null) {
          // If the NACK is in an transaction, dont execute it, add it to the transaction.
          // Execute after Commit
          if(!transidStompframe.containsKey(stompheaders.get("transaction"))) {
            sendError("No Transaction found");
            return;
          }
          List<StompFrame> sf = transidStompframe.get(stompheaders.get("transaction"));
          if(sf == null) sf = new ArrayList<>();
          sf.add(stompframe);
        } else {
          MessageObject mo = WsPool.get().discardMessage(id, stompheaders.get("id"));
          if(mo == null) return;
          addHeader.accept("messageid", mo.getMessageId());
          addHeader.accept("message", mo.getMessage());
          addHeader.accept("wsid", mo.getWebSocketId());
          findAndProcess(Annotation._WS_STOMP_NACK, null, null);
        }
        break;
      case BEGIN:
        transidStompframe.put(stompheaders.get("transaction"), null);
        break;
      case COMMIT:
        commitTransaction(stompheaders.get("transaction"));
        break;
      case ABORT:
        transidStompframe.remove(stompheaders.get("transaction"));
        break;
      case DISCONNECT:
        if(transidStompframe.containsKey(stompheaders.get("transaction")))
          transidStompframe.remove(stompheaders.get("transaction"));

        Map<String, String> ch = new HashMap<>();
        ch.put("receipt-id", stompheaders.get("receipt"));
        ReceiptFrame rf = new ReceiptFrame(Commands.RECEIPT, ch, "");
        super.getSession().getRemote().sendStringByFuture(rf.serializedFrame());
        break;
      default:
        break;
    }
    for(String headername : stompheaders.keySet()) {
      headers.remove(headername);
    }
    headers.remove("messageid");
    headers.remove("message");
    headers.remove("wsid");
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
      if(func != null) new StompResponse(this).create(func, message);
    } catch(final RuntimeException ex) {
      throw ex;
    } catch(final Exception ex) {
      Util.debug(ex);
      throw new CloseException(StatusCode.ABNORMAL, ex.getMessage());
    }
  }
}
