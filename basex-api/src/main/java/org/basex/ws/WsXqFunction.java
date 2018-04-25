package org.basex.ws;

import static org.basex.util.Token.*;

import org.basex.http.restxq.*;
import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.func.*;

/**
 * This class represents a single Websocket-Function.
 * @author BaseX Team 2005-18, BSD License
 * */
public class WsXqFunction implements Comparable<WsXqFunction> {

  /** Associated function. */
  public final StaticFunc function;
  /** Associated module. */
  private final RestXqModule module;
  /** Serialization parameters. */
  public final SerializerOptions output;

  /**
   * Constructor.
   * @param function associated user function
   * @param module associated module
   */
  public WsXqFunction(final StaticFunc function, final QueryContext qc, final RestXqModule module) {
    this.function = function;
    this.module = module;
    output = qc.serParams();
  }

  /**
   * Checks if an WEbsocket request matches this Annotation.
   * @param ann Annotation the annotation parameter
   * @return result of check
   */
  public boolean matches(final Annotation ann) {
    boolean found = false;
    for(Ann checkAnn : function.anns) {
      if(checkAnn.sig == ann) {
        found = true;
      }
    }
    return found;
  }

  /**
   * Checks a function for Websocket and permission annotations.
   * @return {@code true} if function contains relevant annotations
   */
  public boolean parse() {
    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null) continue;
      if(eq(sig.uri, QueryText.WS_URI)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Processes the websocket request.
   * Parses new modules and discards obsolete ones.
   * @param conn Websocket connection
   * @return {@code true} if function creates no result
   * @throws Exception exception
   */
  public boolean process(final WebsocketConnection conn) throws Exception {
    try {
      return module.process(conn, this, null);
    } catch(final QueryException ex) {
      if(ex.file() == null) ex.info(function.info);
      throw ex;
    }
  }

  @Override
  public int compareTo(final WsXqFunction wsxf) {
    // TODO Auto-generated method stub
    return 0;
  }
}
