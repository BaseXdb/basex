package org.basex.http.ws.adapter;

import java.util.*;
import org.basex.http.ws.*;
import org.basex.http.ws.stomp.*;
import org.basex.query.ann.*;

/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class StompWsV10 extends WsAdapter {
  /**
   * The Channelnames where the socket is connected to.
   */
  List<String> channelName;

  /**
   * The Stomp-Id with the corresponding WebsocketUUID - <stompd,websocketUUID>.
   */
  private HashMap<String, String> serverIds;

  /**
   * The subscribtion-ids with the corresponding channel.
   */
  private HashMap<String, String> idchannelMap = new HashMap<>();

  /**
   * The Constructor.
   * @param pPath the Path as String
   */
  public StompWsV10(final String pPath) {
    super();
    path = new WsPath(pPath);
    response = new StompResponse();
  }

  @Override
  public void onWebSocketText(final String message) {
    System.out.println("onWebsocketText: " + message);
    Annotation ann = Annotation._WS_CONNECT;
    // WebsocketMessage wm;

      // Return specific StompFrame here? -> ConnectFrame extend StompFrame,
      // in parse check for all required parameters and set optional also?
      // Maybe bind method in the Frame? or returning list with all parameters to bind?
      // wm = new WebsocketMessage(message);
      StompFrame stompframe = null;
      try {
        stompframe = StompFrame.parse(message);
      } catch(HeadersException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      String stompId;
      String channel;
      switch(stompframe.getCommand()) {
        case SEND:
          ann = Annotation._WS_MESSAGE;
          // Required header
          channel = stompframe.getHeaders().get("destination");
          break;
        case SUBSCRIBE:
          // ann = Annotation._WS_STOMP_SUBSCRIBE;
          // Required Header
          channel = stompframe.getHeaders().get("destination");
          stompId = stompframe.getHeaders().get("id");

          idchannelMap.put(stompId, channel);
          serverIds.put(stompId, WsPool.getInstance().joinChannel(this, channel));
          break;
        case UNSUBSCRIBE:
          // ann = Annotation._WS_STOMP_UNSUBSCRIBE;
          // REquired Header
          stompId = stompframe.getHeaders().get("id");
          channel = idchannelMap.get(stompId);
          idchannelMap.remove(stompId);
          String wsUUID = serverIds.get(stompId);
          serverIds.remove(stompId);
          WsPool.getInstance().removeFromChannel(this, channel, wsUUID);
          break;
        // Transaction stuff
        case BEGIN:
          break;
        case COMMIT:
          break;
        case ABORT:
          break;
        case ACK:
          break;
        case NACK:
          break;
        case DISCONNECT:
          ann = Annotation._WS_CLOSE;
          break;
        case CONNECT:
        case STOMP:
          ann = Annotation._WS_CONNECT;
          break;
        default:
          wsconnection.error("Not a Stomp Command", 500);
          return;
      }
    // findAndProcess(ann, wm);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    // findAndProcess(Annotation._WS_MESSAGE, new WebsocketMessage(payload));
  }

  @Override
  protected void removeWebsocketFromPool() {
    WsPool room = WsPool.getInstance();
    // Remove any Subscriptions
    idchannelMap.forEach((stompId, channel) -> {
      serverIds.remove(stompId);
      room.removeFromChannel(this, channel, serverIds.get(stompId));
    });
  }
}
