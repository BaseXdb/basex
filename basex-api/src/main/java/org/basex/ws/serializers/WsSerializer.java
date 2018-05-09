package org.basex.ws.serializers;

import java.io.*;
import java.util.*;

import org.basex.http.restxq.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.ws.*;

/**
 * Represents the Interface for a WebsocketSerializer.
 * @author BaseX Team 2005-18, BSD License
 */
public interface WsSerializer {

  /**
   * Binds the params of the Function.
   * @param args The Expr
   * @param qc The Querycontext
   * @param message The WebsocketMessage
   * @param wsParameters The list of wsParameters
   * @param function The Staticfunction
   * @param wsfunc The WebsocketFunction which is executing the bind method
   * @throws UnsupportedEncodingException exception
   * @throws QueryException query exception
   */
  void bind(Expr[] args,
            QueryContext qc,
            WebsocketMessage message,
            ArrayList<RestXqParam> wsParameters,
            StaticFunc function,
            WsXqFunction wsfunc) throws QueryException, UnsupportedEncodingException;

  /**
   * Generates the Output and send it to the Client.
   * @param conn The Websocketconnection
   * @param wxf The WebsocetFunction
   * @param qc the Querycontext
   * @return {@code true} if function creates no result
   * @throws QueryException Query exception
   * @throws IOException  exception
   */
  boolean generateOutput(WebsocketConnection conn,
                         WsXqFunction wxf,
                         QueryContext qc) throws IOException, QueryException;
}
