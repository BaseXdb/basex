package org.basex.ws;

import java.util.*;
import javax.validation.constraints.*;
import org.basex.http.restxq.*;
import org.basex.query.ann.*;
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

    @Override
    public void onWebSocketConnect(@NotNull final Session sess)
    {
        // Sets Session and Remote in Superclass
        super.onWebSocketConnect(sess);

        // Get the ChannelName if it is set
        UpgradeRequest req = sess.getUpgradeRequest();
        channelName = req.getParameterMap().get("channelName");

        // Get the Accepted Subprotocols
        UpgradeResponse resp = sess.getUpgradeResponse();
        subprotocol = resp.getAcceptedSubProtocol();

        // Add to the WebSocketRoom
        Room.getInstance().join(this);

        // Create new WebsocketConnection
        wsconnection =
            new WebsocketConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess);

        final RestXqModules rxm = RestXqModules.get(wsconnection.context);

        // select the closest match for this request
        WsXqFunction func = null;
        try {
           func = rxm.find(wsconnection, null, Annotation._WS_CONNECT);
           if(func != null)
             func.process(wsconnection);
        } catch(Exception e) {
          e.printStackTrace();
        }

        // Just for Logging purpose
        System.out.println("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(final String message)
    {
        // Just for Logging purpose
        System.out.println("Received TEXT message: " + message);

        final RestXqModules rxm = RestXqModules.get(wsconnection.context);

        // select the closest match for this request
        WsXqFunction func = null;
           try {
              func = rxm.find(wsconnection, null, Annotation._WS_MESSAGE);
              if(func != null)
                func.process(wsconnection);
           } catch(Exception e) {
             e.printStackTrace();
           }
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason)
    {
      final RestXqModules rxm = RestXqModules.get(wsconnection.context);

      // select the closest match for this request
      WsXqFunction func = null;
         try {
            func = rxm.find(wsconnection, null, Annotation._WS_CLOSE);
            if(func != null)
              func.process(wsconnection);
         } catch(Exception e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }

      // Resets Session and Remote in Superclass
        super.onWebSocketClose(statusCode, reason);

        // Just for Loggingpurpose
        System.out.println("Socket Closed: [" + statusCode + "] " + reason);

        // Remove the user from the Room
        Room.getInstance().remove(this);
    }

    @Override
    public void onWebSocketError(final Throwable cause)
    {
        cause.printStackTrace(System.err);
    }
}