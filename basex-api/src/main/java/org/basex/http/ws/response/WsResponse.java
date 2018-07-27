package org.basex.http.ws.response;

import java.io.*;
import org.basex.http.ws.*;
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
   * @param conn The WsConnection
   * @param wxf The WsFunction
   * @param qc The QueryContext
   * @throws IOException IOException
   * @throws QueryException QueryException
   */
  void create(WsConnection conn, WsFunction wxf, QueryContext qc)
      throws IOException, QueryException;
}
