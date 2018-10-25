package org.basex.http.ws;

import static org.basex.http.web.WebText.*;
import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
import org.basex.http.restxq.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents a single WebSocket function.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Johannes Finckh
 */
public final class WsFunction extends WebFunction {
  /** Path of the function. */
  public WsPath path;
  /** Message parameter. */
  public WebParam message;

  /**
   * Constructor.
   * @param function associated user function
   * @param qc query context
   * @param module web module
   */
  public WsFunction(final StaticFunc function, final QueryContext qc, final WebModule module) {
    super(function, module, qc);
  }

  @Override
  public boolean parse(final Context ctx) throws QueryException {
    final boolean[] declared = new boolean[function.params.length];
    // counter for annotations that should occur only once
    boolean found = false;
    int count = 0;

    for(final Ann ann : function.anns) {
      final Annotation sig = ann.sig;
      if(sig == null || (!eq(sig.uri, QueryText.WS_URI) && !eq(sig.uri, QueryText.WS_STOMP_URI)) ) continue;
      found = true;
      final Item[] args = ann.args();
      switch(sig) {
        case _WS_HEADER_PARAM:
          final String name = toString(args[0]);
          final QNm var = checkVariable(toString(args[1]), declared);
          final int al = args.length;
          final ItemList items = new ItemList(al - 2);
          for(int a = 2; a < al; a++) items.add(args[a]);
          headerParams.add(new WebParam(var, name, items.value()));
          break;
        case _WS_CLOSE:
        case _WS_CONNECT:
        case _WS_STOMP_CONNECT:
        case _WS_STOMP_SUBSCRIBE:
        case _WS_STOMP_UNSUBSCRIBE:
          path = new WsPath(toString(args[0]));
          count++;
          break;
        case _WS_ERROR:
        case _WS_MESSAGE:
        case _WS_STOMP_MESSAGE:
          final QNm msg = checkVariable(toString(args[1]), declared);
          message = new WebParam(msg, "message", null);
          path = new WsPath(toString(args[0]));
          count++;
          break;
        case _WS_STOMP_NACK:
        case _WS_STOMP_ACK:
          count++;
          break;
        default:
          break;
      }
    }

    if(found) {
      if(count == 0) throw error(function.info, ANN_MISSING);
      if(count > 1) throw error(function.info, ANN_CONFLICT);
      final int dl = declared.length;
      for(int d = 0; d < dl; d++) {
        if(!declared[d]) throw error(function.info, VAR_UNDEFINED_X,
            function.params[d].name.string());
      }
    }
    return found;
  }

  /**
   * Binds the function parameters.
   * @param args arguments
   * @param msg message (can be {@code null}; otherwise string or byte array)
   * @param values header values
   * @param qc query context
   * @throws QueryException query exception
   */
  public void bind(final Expr[] args, final Object msg, final Map<String, Value> values,
      final QueryContext qc) throws QueryException {

    if(msg != null) values.put("message", msg instanceof byte[] ?
      B64.get((byte[]) msg) : Str.get((String) msg));

    for(final WebParam rxp : headerParams) {
      bind(rxp.var, args, values.get(rxp.name), qc);
    }
    if(message != null) {
      bind(message.var, args, values.get(message.name), qc);
    }
  }

  /**
   * Checks if an WebSocket request matches this annotation and path.
   * @param ann annotation (can be {@code null})
   * @param pth path to compare to (can be {@code null})
   * @return boolean result of check
   */
  public boolean matches(final Annotation ann, final WsPath pth) {
    for(final Ann a : function.anns) {
      if((ann == null || a.sig == ann) && (pth == null || path == null || path.compareTo(pth) == 0)) return true;
    }
    return false;
  }

  @Override
  public int compareTo(final WebFunction wsxf) {
    return wsxf instanceof RestXqFunction ? path.compareTo(((WsFunction) wsxf).path) : 1;
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  @Override
  public QueryException error(final String msg, final Object... ext) {
    return error(function.info, Util.info(msg, ext));
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates an exception with the specific message.
   * @param info the StaticFunc info
   * @param msg the message
   * @param ext error extension
   * @return exception
   */
  private static QueryException error(final InputInfo info, final String msg, final Object... ext) {
    return BASEX_WS_X.get(info, Util.info(msg, ext));
  }
}
