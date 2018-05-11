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
      Annotation ann = Annotation._WS_CONNECT;
      WebsocketMessage wm;

      // Check if the Protocol is stomp
      if(this.subprotocol.equals("v10.stomp")) {
        //TODO Stompframe here
        wm = new WebsocketMessage(message);
        // If it is Stomp, parse the message to a StompFrame
        StompFrame stompframe = StompFrame.parse(message);
        // Check the Command of the Stompframe and search a different
        // Annotation per Command
        switch(stompframe.getCommand()) {
          case SEND:
            break;
          case SUBSCRIBE:
            break;
          case UNSUBSCRIBE:
            break;
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