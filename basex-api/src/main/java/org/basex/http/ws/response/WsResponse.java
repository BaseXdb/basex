package org.basex.http.ws.response;

import java.io.*;
import org.basex.http.ws.*;
import org.basex.query.*;

/**
 * Represents the Interface for a WebsocketSerializer.
 * @author BaseX Team 2005-18, BSD License
 */
public interface WsResponse {
  /**
   * Creates the Response.
   * @param conn The WsConnection
   * @param wxf The WsFunction
   * @param qc The QueryContext
   * @return Boolean
   * @throws IOException IOException
   * @throws QueryException QueryException
   */
  boolean create(WsConnection conn, WsFunction wxf, QueryContext qc)
      throws IOException, QueryException;
}
