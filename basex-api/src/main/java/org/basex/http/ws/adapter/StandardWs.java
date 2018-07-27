package org.basex.http.ws.adapter;

import org.basex.http.ws.*;
import org.basex.http.ws.response.*;
import org.basex.query.ann.*;

/**
 * This class defines a standard WebSocket.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class StandardWs extends WsAdapter {
  /**
   * Constructor.
   * @param path path string
   */
  public StandardWs(final String path) {
    this.path = new WsPath(path);
    response = new StandardWsResponse();
  }

  @Override
  public void onWebSocketText(final String message) {
    findAndProcess(Annotation._WS_MESSAGE, message, headers);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    headers.put("offset", Integer.toString(offset));
    headers.put("len", Integer.toString(len));
    headers.put("id", id);
    findAndProcess(Annotation._WS_MESSAGE, payload, headers);
  }

  @Override
  protected void removeWebSocket() {
    final WsPool pool = WsPool.get();
    if(path == null) {
      pool.remove(id);
    } else {
      pool.removeFromChannel(this, path.toString(), id);
    }
  }
}
