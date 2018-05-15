package org.basex.ws;

import java.util.*;
import javax.validation.constraints.*;
import org.basex.http.restxq.*;
import org.basex.query.ann.*;
import org.basex.ws.serializers.*;
import org.basex.ws.serializers.stomp.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class WebSocke extends WebSocketAdapter
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
    private WebsocketConnection wsconnection;
    /**
     * The Serializer for specific Subprotocols.
     * */
    private WsSerializer serializer;
    /**
     * The subscribtion-ids with the corresponding channel.
     * */
    private HashMap<String, String> idchannelMap = new HashMap<>();

    /**
     * Constructor.
     * @param subprotocol String subprotocol
     * */
    public WebSocke(final String subprotocol) {
      this.subprotocol = subprotocol;
      setSerializer();
    }

    /**
     * Sets the Serializer.
     */
    private void setSerializer() {
      if(this.subprotocol == null) {
        this.serializer = new WsStandardSerializer();
      } else if("v10.stomp".equals(this.subprotocol)) {
        this.serializer = new StompSerializer();
      }
    }

    @Override
    public void onWebSocketConnect(@NotNull final Session sess)
    {
        // Sets Session and Remote in Superclass
        super.onWebSocketConnect(sess);

        // Get the ChannelName if it is set
        UpgradeRequest req = sess.getUpgradeRequest();
        channelName = req.getParameterMap().get("channelName");

        // Add to the WebSocketRoom
        Room.getInstance().join(this);

        // Create new WebsocketConnection
        wsconnection =
            new WebsocketConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess);

        // If the STOMP-Protocol is used, no connect-execution in WebsocketConnect.
        // The XQuery connectfunction in the stompprotocol is executed within the
        // CONNECT-Frame (onWebSocketText)
        if(!this.subprotocol.equals("v10.stomp")) {
          findAndProcess(Annotation._WS_CONNECT, null);
        }
    }

    @Override
    public void onWebSocketText(final String message)
    {
      System.out.println("onWebsocketText: " + message);
      Annotation ann = Annotation._WS_CONNECT;
      WebsocketMessage wm;

      // Check if the Protocol is stomp
      if(this.subprotocol.equals("v10.stomp")) {
        //TODO Stompframe here
        wm = new WebsocketMessage(message);
        // If it is Stomp, parse the message to a StompFrame
        // Return specific StompFrame here? -> ConnectFrame extend StompFrame,
        // in parse check for all required parameters and set optional also?
        // Maybe bind method in the Frame? or returning list with all parameters to bind?
        StompFrame stompframe = null;
        try {
          stompframe = StompFrame.parse(message);
        } catch(HeadersException e) {
          wsconnection.error(e.getMessage(), 500);
        }
        String id;
        String channel;
        switch(stompframe.getCommand()) {
          case SEND:
            ann = Annotation._WS_MESSAGE;
            // Required header
            channel = stompframe.getHeaders().get("destination");
            // Optional Header
            String contentType = stompframe.getHeaders().get("content-type");
            String contentLength = stompframe.getHeaders().get("content-length");
            String transaction = stompframe.getHeaders().get("transaction");

            break;
          case SUBSCRIBE:
            ann = Annotation._WS_STOMP_SUBSCRIBE;
            // Required Header
            channel = stompframe.getHeaders().get("destination");
            id = stompframe.getHeaders().get("id");
            // Optional Header
            String ack = stompframe.getHeaders().get("ack");

            idchannelMap.put(id, channel);
            Room.getInstance().joinChannel(this, channel);
            break;
          case UNSUBSCRIBE:
            ann = Annotation._WS_STOMP_UNSUBSCRIBE;
            // REquired Header
            id = stompframe.getHeaders().get("id");
            channel = idchannelMap.get(id);
            idchannelMap.remove(id);
            Room.getInstance().removeFromChannel(this, channel);
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
            // Optional Headers
            String receipt = stompframe.getHeaders().get("receipt");
            String receiptId = stompframe.getHeaders().get("receiptId");
            break;
          case CONNECT:
          case STOMP:
            ann = Annotation._WS_CONNECT;
            // Required Headers
            String acceptVersion = stompframe.getHeaders().get("accept-version");
            String host = stompframe.getHeaders().get("host");
            // Optional Headers
            String login = stompframe.getHeaders().get("login");
            String passcode = stompframe.getHeaders().get("passcode");
            String heartBeat = stompframe.getHeaders().get("heart-beat");
            break;
          default:
              wsconnection.error("Not a Stomp Command", 500);
              return;
        }
      } else {
        wm = new WebsocketMessage(message);
      }
      findAndProcess(ann, wm);
    }

    @Override
    public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
      findAndProcess(Annotation._WS_MESSAGE, new WebsocketMessage(payload));
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason)
    {
      Room room = Room.getInstance();
      // Remove any Subscriptions
      idchannelMap.forEach((id, channel) -> room.removeFromChannel(this, channel));

      findAndProcess(Annotation._WS_CLOSE, null);

      // Resets Session and Remote in Superclass
        super.onWebSocketClose(statusCode, reason);

        // Remove the user from the Room
        Room.getInstance().remove(this);
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
    private void findAndProcess(final Annotation ann, final WebsocketMessage msg) {
      final RestXqModules rxm = RestXqModules.get(wsconnection.context);

      // select the closest match for this request
      WsXqFunction func = null;
         try {
            func = rxm.find(wsconnection, null, ann);
            if(func != null && serializer != null)
              func.process(wsconnection, msg, serializer);
         } catch(Exception e) {
           wsconnection.error(e.getMessage(), 500);
         }
    }
}