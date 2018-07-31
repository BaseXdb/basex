package org.basex.http.ws.response;

import java.io.*;

import org.basex.http.ws.*;
import org.basex.http.ws.adapter.*;
import org.basex.query.*;

/**
 * Represents the interface for a WebSocket response. The responses can be different for
 * different sub-protocols.
 *
 * @author Johannes Finckh
 * @author BaseX Team 2005-18, BSD License
 */
public interface WsResponse {
  /**
   * Creates the Response.
   * @param ws WebSocket
   * @param wxf function to be called
   * @param qc query context
   * @throws IOException I/O Exception
   * @throws QueryException query exception
   */
  void create(WsAdapter ws, WsFunction wxf, QueryContext qc) throws IOException, QueryException;
}
