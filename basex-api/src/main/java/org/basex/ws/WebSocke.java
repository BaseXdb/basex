package org.basex.ws;

import java.util.*;
import javax.validation.constraints.*;
import org.basex.http.restxq.*;
import org.basex.query.ann.*;
import org.basex.ws.serializers.*;
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
     * Sets the Subprotocol in the Response. Uses the first matching Protocol and sets it.
     * @param sess Session
     */
    private void setSubprotocol(final Session sess) {
        List<String> subprotocols = sess.getUpgradeRequest().getSubProtocols();
        // If Client gives no subprotocol no one has to be accepted
        if(subprotocols.size() == 0) {
          serializer = new WsSerializer();
          return;
          }

        for(String sp: subprotocols) {
          if(sp.equals("stomp")) {
            subprotocol = sp;
            sess.getUpgradeResponse().setAcceptedSubProtocol(sp);
            // serializer = new StompSerializer();
            return;
          }
        }

        // If no subprotocol is supported, close connection with error
        // TODO
    }


    @Override
    public void onWebSocketConnect(@NotNull final Session sess)
    {
        // Sets Session and Remote in Superclass
        super.onWebSocketConnect(sess);

        // Get the ChannelName if it is set
        UpgradeRequest req = sess.getUpgradeRequest();
        channelName = req.getParameterMap().get("channelName");

        setSubprotocol(sess);

        // Add to the WebSocketRoom
        Room.getInstance().join(this);

        // Create new WebsocketConnection
        wsconnection =
            new WebsocketConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess);

        findAndProcess(Annotation._WS_CONNECT, null);
    }

    @Override
    public void onWebSocketText(final String message)
    {
      findAndProcess(Annotation._WS_MESSAGE, new WebsocketMessage(message));
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