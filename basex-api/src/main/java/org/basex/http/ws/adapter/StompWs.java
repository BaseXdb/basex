package org.basex.http.ws.adapter;

import java.util.*;
import javax.validation.constraints.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.http.ws.stomp.*;
import org.basex.query.ann.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class StompWs extends WebSocketAdapter
{
    /**
     * The Channelname.
     */
    List<String> channelName;

    /**
     * The accepted Subprotocols.
     * */
    String subprotocol;
    /**
     * The Websocketconnection.
     */
    private WsConnection wsconnection;
    /**
     * The Serializer for specific Subprotocols.
     * */
    private WsResponse serializer;

    /**
     * The Stomp-Id with the corresponding server unique id - <stompd,serverid>
     * */
    private HashMap<String, String> serverIds;

    /**
     * The subscribtion-ids with the corresponding channel.
     * */
    private HashMap<String, String> idchannelMap = new HashMap<>();

    /**
     * The unigque serverid from the initial connect.
     * */
    private String serverId;

    /**
     * Constructor.
     * @param subprotocol String subprotocol
     * */
    public StompWs(final String subprotocol) {
      this.subprotocol = subprotocol;
      setSerializer();
    }

    /**
     * Sets the Serializer.
     */
    private void setSerializer() {
      if(this.subprotocol == null) {
        this.serializer = new WsStandardResponse();
      } else if("v10.stomp".equals(this.subprotocol)) {
        this.serializer = new StompResponse();
      }
    }

    @Override
    public void onWebSocketConnect(@NotNull final Session sess)
    {
        // Sets Session and Remote in Superclass
        super.onWebSocketConnect(sess);

        // Add to the WebSocketRoom
        serverId = WsPool.getInstance().join(this);

        // Create new WebsocketConnection
//        wsconnection =
//            new WsConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess);

        // If the STOMP-Protocol is used, no connect-execution in WebsocketConnect.
        // The XQuery connectfunction in the stompprotocol is executed within the
        // CONNECT-Frame (onWebSocketText)
        if(!this.subprotocol.equals("v10.stomp")) {
//          findAndProcess(Annotation._WS_CONNECT, null);
        }
    }

    @Override
    public void onWebSocketText(final String message)
    {
      System.out.println("onWebsocketText: " + message);
      Annotation ann = Annotation._WS_CONNECT;
//      WebsocketMessage wm;

      // Check if the Protocol is stomp
      if(this.subprotocol.equals("v10.stomp")) {
        // If it is Stomp, parse the message to a StompFrame
        // Return specific StompFrame here? -> ConnectFrame extend StompFrame,
        // in parse check for all required parameters and set optional also?
        // Maybe bind method in the Frame? or returning list with all parameters to bind?
//        wm = new WebsocketMessage(message);
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
//            ann = Annotation._WS_STOMP_SUBSCRIBE;
            // Required Header
            channel = stompframe.getHeaders().get("destination");
            stompId = stompframe.getHeaders().get("id");

            idchannelMap.put(stompId, channel);
            serverIds.put(stompId,WsPool.getInstance().joinChannel(this, channel));
            break;
          case UNSUBSCRIBE:
//            ann = Annotation._WS_STOMP_UNSUBSCRIBE;
            // REquired Header
            stompId = stompframe.getHeaders().get("id");
            channel = idchannelMap.get(stompId);
            idchannelMap.remove(stompId);
            String sId = serverIds.get(stompId);
            serverIds.remove(stompId);
            WsPool.getInstance().removeFromChannel(this, channel,sId);
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
      } else {
//        wm = new WebsocketMessage(message);
      }
//      findAndProcess(ann, wm);
    }

    @Override
    public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
//      findAndProcess(Annotation._WS_MESSAGE, new WebsocketMessage(payload));
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason)
    {
      WsPool room = WsPool.getInstance();
      // Remove any Subscriptions
      idchannelMap.forEach((stompId, channel) -> {
        String sId = serverIds.get(stompId);
        serverIds.remove(stompId);
        room.removeFromChannel(this, channel, sId);
      });

//      findAndProcess(Annotation._WS_CLOSE, null);

      // Resets Session and Remote in Superclass
        super.onWebSocketClose(statusCode, reason);

        // Remove the user from the Room
        WsPool.getInstance().remove(serverId);
    }

    @Override
    public void onWebSocketError(final Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

    /**
     * Finds a WSFunction and processes it.
     * @param ann The Websocketannotation
     * @param msg The Message
     */
    private void findAndProcess(final Annotation ann, final Object msg) {
      final RestXqModules rxm = RestXqModules.get(wsconnection.context);

      // select the closest match for this request
      WsFunction func = null;
         try {
//            func = rxm.find(wsconnection, ann, new WsPath(""));
            if(func != null && serializer != null)
              func.process(wsconnection, msg, serializer, null);
         } catch(Exception e) {
           wsconnection.error(e.getMessage(), 500);
         }
    }
}