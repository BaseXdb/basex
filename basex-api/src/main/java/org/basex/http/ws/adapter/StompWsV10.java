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
   * The Stomp-Id with the corresponding WebsocketUUID - <stompd,websocketUUID>.
   */
  private HashMap<String, String> serverIds = new HashMap<>();

  /**
   * The subscribtion-ids with the corresponding channel.
   */
  private HashMap<String, String> idchannelMap = new HashMap<>();

  /**
   * The Main Path of this Websocket (the path the user connected to).
   * */
  private final String mainPath;
  /**
   * The Constructor.
   * @param pPath the Path as String
   */
  public StompWsV10(final String pPath) {
    super();
    path = new WsPath(pPath);
    response = new StompResponse();
    mainPath = pPath;
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
      wsconnection.error(e.getMessage(), 500);
    }
    return stompframe;
  }

  @Override
  public void onWebSocketText(final String message) {
    Annotation ann = Annotation._WS_MESSAGE;
    StompFrame stompframe = parseStompFrame(message);
    if(stompframe == null) return;

    String stompId;
    // Put the STOMP-Command to the HeaderParams -> The User can decide what to do
    headerParams.put("STOMPCommand", stompframe.getCommand().toString());
    // Get all HeaderParams detected by the stompframe and add it to headerParams for binding it
    stompframe.appendHeader(headerParams);

    String channel = stompframe.getHeaders().get("destination");
    if(channel != null) {
      wsconnection.updatePath(channel);
    } else {
      wsconnection.updatePath(mainPath);
    }
    // Check which Command is set by the StompFrame
    switch(stompframe.getCommand()) {
      case SEND:
        ann = Annotation._WS_MESSAGE;
        break;
      case SUBSCRIBE:
        // Get Headers needed for the Channel-Maps
        stompId = stompframe.getHeaders().get("id");
        // Add to the ID-Channel Map (Stomp-id from the Client, Channel)
        idchannelMap.put(stompId, channel);
        // Add the Stomp id to the map: stompID - WSuuid (returned from the WSPool
        serverIds.put(stompId, WsPool.getInstance().joinChannel(this, channel));
        break;
      case UNSUBSCRIBE:
        // Required Header
        stompId = stompframe.getHeaders().get("id");
        channel = idchannelMap.get(stompId);

        if(channel != null) {
          wsconnection.updatePath(channel);
        } else {
          wsconnection.updatePath(mainPath);
        }

        // Remove the StompId with the channel from the idchannelMap
        idchannelMap.remove(stompId);
        // Get the Id of the WebsocketInstance in the Room and remove it from serverIds
        String wsUUID = serverIds.get(stompId);
        serverIds.remove(stompId);
        // Remove the WSInstance from the WebSocketPool
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
      case DISCONNECT:
        ann = Annotation._WS_CLOSE;
        this.removeWebsocketFromPool();
        break;
      case CONNECT:
        // Dont Call ws_connect twice
        // ann = Annotation._WS_CONNECT;
        return;
      default:
        wsconnection.error("Not a Stomp Command", 500);
        return;
    }
    findAndProcess(ann, stompframe.getBody(), headerParams);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    wsconnection.error("Plain Binary Messages are not supported by the STOMP-Protocol. "
        + "Try to send a regualar STOMP-Message, add a content-type HEADER and "
        + "send your Binary-Data as Message-Body", 500);
  }

  @Override
  protected void removeWebsocketFromPool() {
    WsPool room = WsPool.getInstance();
    // Remove any Subscriptions
    idchannelMap.forEach((stompId, channel) -> {
      room.removeFromChannel(this, channel, serverIds.get(stompId));
      serverIds.remove(stompId);
    });
    room.removeFromChannel(this, mainPath, id);
  }
}
