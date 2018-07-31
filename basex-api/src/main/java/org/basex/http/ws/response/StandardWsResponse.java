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

    // don't send anything if WebSocket gets closed because the connection is already closed
    // [JF] Maybe we should disallow results for functions annotated with %ws:close
    if(wxf.matches(Annotation._WS_CLOSE)) return;

    for(final Object value : WsPool.serialize(qc.iter(), wxf.output)) {
      if(value instanceof byte[]) {
        ws.session.getRemote().sendBytes(ByteBuffer.wrap((byte[]) value));
      } else {
        ws.session.getRemote().sendString((String) value);
      }
    }
  }
}
