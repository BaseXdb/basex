package org.basex.http.ws.response;

import java.io.*;
import java.nio.*;

import org.basex.http.ws.*;
import org.basex.io.out.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;

/**
 * Represents the standard serializer for WebsocketMessages.
 * @author BaseX Team 2005-18, BSD License
 */
public class WsStandardResponse implements WsResponse {
  @Override
  public boolean create(final WsConnection conn,
                                final WsFunction wxf, final QueryContext qc)
                 throws IOException, QueryException {
    ArrayOutput ao = new ArrayOutput();
    Serializer ser = Serializer.get(ao, wxf.output);
    Iter iter = qc.iter();

    for(Item it; (it = iter.next()) != null;) {
      // Dont send anything if Websocket gets closed
      if(wxf.matches(Annotation._WS_CLOSE)) continue;

      ser.reset();
      ser.serialize(it);
      if(it instanceof Bin) {
        //final byte[] bytes = ((Bin) it).binary(null);
        final byte[] bytes = ao.toArray();
        conn.sess.getRemote().sendBytes(ByteBuffer.wrap(bytes));
      } else {
        conn.sess.getRemote().sendString(ao.toString());
      }
      ao.reset();
    }
    return true;
  }
}
