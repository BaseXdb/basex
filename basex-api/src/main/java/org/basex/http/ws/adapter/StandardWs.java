package org.basex.http.ws.adapter;

import javax.servlet.http.*;

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
   * @param req request
   */
  public StandardWs(final HttpServletRequest req) {
    super(req);
    response = new StandardWsResponse();
  }

  @Override
  public void onWebSocketText(final String message) {
    findAndProcess(Annotation._WS_MESSAGE, message);
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset, final int len) {
    // [JF] If offset is always 0, and if len = payload.length, we should drop these two as well
    // [JF] What is the default for binary data? 1TB?
    // [JF] What happens if the default is exceeded?
    // [JF] What is the name of the property for limiting the binary limit?
    //      Is it called similar to maxTextMessageSize?
    findAndProcess(Annotation._WS_MESSAGE, payload);
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
