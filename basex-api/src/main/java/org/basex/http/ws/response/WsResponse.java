package org.basex.http.ws.response;

import java.io.*;
import java.util.*;

import org.basex.http.util.*;
import org.basex.http.ws.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;

/**
 * Represents the Interface for a WebsocketSerializer.
 * @author BaseX Team 2005-18, BSD License
 */
public interface WsResponse {

  /**
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The Querycontext
   * @param message The WebsocketMessage
   * @param wsParameters The list of wsParameters
   * @param function The Staticfunction
   * @param wsfunc The WebsocketFunction which is executing the bind method
   * @param header The header to set
   * @throws UnsupportedEncodingException exception
   * @throws QueryException query exception
   */
  void bind(Expr[] args,
            QueryContext qc,
            Object message,
            ArrayList<WebParam> wsParameters,
            StaticFunc function,
            WsFunction wsfunc,
            Map<String, String> header) throws QueryException, UnsupportedEncodingException;

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
