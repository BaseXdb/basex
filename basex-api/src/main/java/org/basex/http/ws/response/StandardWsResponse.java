package org.basex.http.ws.response;

import java.io.*;
import java.nio.*;

import org.basex.http.ws.*;
import org.basex.http.ws.adapter.*;
import org.basex.query.*;
import org.basex.query.ann.*;

/**
 * Represents the standard serializer for WebSocket messages.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public class StandardWsResponse implements WsResponse {
  @Override
  public void create(final WsAdapter ws, final WsFunction wxf, final QueryContext qc)
      throws IOException, QueryException {

    for(final Object value : WsPool.serialize(qc.iter(), wxf.output)) {
      // don't send anything if WebSocket gets closed because the connection is already closed
      // We have to do this check inside the loop because the XQuery function should get executed
      // too if it is a _WS_CLOSE function, just don't return a result.
      if(wxf.matches(Annotation._WS_CLOSE)) continue;

      if(value instanceof byte[]) {
        ws.getSession().getRemote().sendBytes(ByteBuffer.wrap((byte[]) value));
      } else {
        ws.getSession().getRemote().sendString((String) value);
      }
    }
  }
}
