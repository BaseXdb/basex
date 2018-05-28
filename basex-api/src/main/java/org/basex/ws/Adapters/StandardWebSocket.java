package org.basex.ws.Adapters;

import java.util.*;

import javax.validation.constraints.*;
import org.basex.http.restxq.*;
import org.basex.query.ann.*;
import org.basex.ws.*;
import org.basex.ws.serializers.*;
import org.eclipse.jetty.websocket.api.*;

/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class StandardWebSocket extends WebSocketAdapter
{
    /**
     * The Websocketconnection.
     */
    private WebsocketConnection wsconnection;

    /**
     * The StandardSerializer.
     * */
    private WsSerializer serializer = new WsStandardSerializer();

    @Override
    public void onWebSocketConnect(@NotNull final Session sess)
    {
        // Sets Session and Remote in Superclass
        super.onWebSocketConnect(sess);

        UpgradeRequest ur = sess.getUpgradeRequest();

        // Add Headers (for binding them to the XQueryParameters in the
        // corresponding bind Method
        Map<String, String> header = new HashMap<>();
        header.put("Http-Version", ur.getHttpVersion());
        header.put("Origin", ur.getOrigin());
        header.put("Protocol-version", ur.getProtocolVersion());
        header.put("QueryString", ur.getQueryString());
        header.put("IsSecure", String.valueOf(ur.isSecure()));
        header.put("RequestURI", sess.getUpgradeRequest().getRequestURI().toString());
        // The Headers of the upgraderequest we are interested in
        List<String> headerKeys = new ArrayList<>();
        Collections.addAll(headerKeys, "Host", "Sec-WebSocket-Version");
        sess.getUpgradeRequest().getHeaders().forEach((k, v) -> {
          if(headerKeys.contains(k)) {
            header.put(k, v.get(0));
          }
        });;

        // Add to the WebSocketRoom
        Room.getInstance().join(this);

        // Create new WebsocketConnection
        wsconnection =
            new WebsocketConnection(sess.getUpgradeRequest(), sess.getUpgradeResponse(), sess);

        findAndProcess(Annotation._WS_CONNECT, null, header);
    }

    @Override
    public void onWebSocketText(final String message)
    {
      Annotation ann = Annotation._WS_MESSAGE;
      WebsocketMessage wm = new WebsocketMessage(message);
      findAndProcess(ann, wm, null);
    }

    @Override
    public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
      Map<String, String> header = new HashMap<>();
      header.put("offset", "" + offset);
      header.put("len", "" + len);
      findAndProcess(Annotation._WS_MESSAGE, new WebsocketMessage(payload), header);
    }

    @Override
    public void onWebSocketClose(final int statusCode, final String reason)
    {
      findAndProcess(Annotation._WS_CLOSE, null, null);

      // Resets Session and Remote in Superclass
      super.onWebSocketClose(statusCode, reason);

      // Remove the user from the Room
      Room.getInstance().remove(this);
    }

    /*
     * This is a way for the internal implementation to notify of
     * exceptions occured during the processing of websocket.
     * Usually this occurs from bad / malformed incoming packets.
     * (example: bad UTF8 data, frames that are too big, violations of the spec).
     * This will result in the Session being closed by the implementing side.
     */
    @Override
    public void onWebSocketError(final Throwable cause)
    {
        wsconnection.error(cause.toString(), 500);
        cause.printStackTrace(System.err);
        super.getSession().close();
    }

    /**
     * Finds a WSFunction and processes it.
     * @param ann The Websocketannotation
     * @param msg The Message
     * @param header The headers to set
     */
    private void findAndProcess(final Annotation ann, final WebsocketMessage msg,
        final Map<String, String> header) {
      final RestXqModules rxm = RestXqModules.get(wsconnection.context);

      // select the closest match for this request
      WsXqFunction func = null;
         try {
            func = rxm.find(wsconnection, null, ann);
            if(func != null && serializer != null)
              func.process(wsconnection, msg, serializer, header);
         } catch(Exception e) {
           e.printStackTrace();
           wsconnection.error("Find and Process" + e.getMessage(), 500);
         }
    }
}