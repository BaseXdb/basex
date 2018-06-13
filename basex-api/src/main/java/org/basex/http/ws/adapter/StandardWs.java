package org.basex.http.ws.adapter;

import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;


/**
 * This class defines a Websocket. It implements WebsocketAdapter of jetty-native-websockets.
 * Remeber: Each Client has its own instance of this Websocket!
 *
 * @author BaseX Team 2005-18, BSD License
 */
public class StandardWs extends WsAdapter {
  /**
   * Constructor.
   * @param pPath as a String
   */
  public StandardWs(final String pPath) {
    super();
    path = new WsPath(pPath);
    response = new WsStandardResponse();
  }

  @Override
  public void onWebSocketText(final String message) {
    Annotation ann = Annotation._WS_MESSAGE;
    findAndProcess(ann, message, headerParams);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    headerParams.put("offset", "" + offset);
    headerParams.put("len", "" + len);
    headerParams.put("id", id);
    findAndProcess(Annotation._WS_MESSAGE, payload, headerParams);
  }

  @Override
  protected void removeWebsocketFromPool() {
    if(path == null) {
      WsPool.getInstance().remove(id);
    } else {
      WsPool.getInstance().removeFromChannel(this, path.toString(), id);
    }
  }
}
