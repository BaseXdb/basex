package org.basex.ws;

import java.util.*;

import javax.validation.constraints.*;

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

        // Just for Logging purpose
        System.out.println("Socket Connected: " + sess);
    }

    @Override
    public void onWebSocketText(final String message)
    {
        // does nothing in superclass?!
        // super.onWebSocketText(message);

        // Just for Logging purpose
        System.out.println("Received TEXT message: " + message);

        // Broadcast the Message to all connected instances
        Room.getInstance().broadcast(message);
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason)
    {
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
        // Does nothing in superclass?!
        //super.onWebSocketError(cause);
        cause.printStackTrace(System.err);
    }
}