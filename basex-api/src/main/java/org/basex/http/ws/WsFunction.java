package org.basex.http.ws;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.util.*;

import org.basex.core.*;
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
 * @author BaseX Team 2005-23, BSD License
 * @author Johannes Finckh
 */
public final class WsFunction extends WebFunction {
  /** Path of the function. */
  public WsPath path;
  /** Message parameter. */
  private WebParam message;

  /**
   * Constructor.
   * @param function associated user function
   * @param module web module
   * @param qc query context
   */
  public WsFunction(final StaticFunc function, final WebModule module, final QueryContext qc) {
    super(function, module, qc);
  }

  @Override
  public boolean parseAnnotations(final Context ctx) throws QueryException {
    final boolean[] declared = new boolean[function.arity()];
    // counter for annotations that should occur only once
    boolean found = false;
    final AnnList starts = new AnnList();

    for(final Ann ann : function.anns) {
      final Annotation def = ann.definition;
      if(def == null || !eq(def.uri, QueryText.WS_URI)) continue;

      found = true;
      final Value value = ann.value();
      switch(def) {
        case _WS_HEADER_PARAM:
          final String name = toString(value.itemAt(0));
          final QNm var = checkVariable(toString(value.itemAt(1)), declared);
          final long vs = value.size();
          final ItemList items = new ItemList(vs - 2);
          for(int v = 2; v < vs; v++) items.add(value.itemAt(v));
          headerParams.add(new WebParam(var, name, items.value()));
          break;
        case _WS_CLOSE:
        case _WS_CONNECT:
          path = new WsPath(toString(value.itemAt(0)));
          starts.add(ann);
          break;
        case _WS_ERROR:
        case _WS_MESSAGE:
          final QNm msg = checkVariable(toString(value.itemAt(1)), declared);
          message = new WebParam(msg, "message", null);
          path = new WsPath(toString(value.itemAt(0)));
          starts.add(ann);
          break;
        default:
          break;
      }
    }
    return checkParsed(found, starts, declared);
  }

  /**
   * Binds the function parameters.
   * @param msg message (can be {@code null}; otherwise string or byte array)
   * @param values header values
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  Expr[] bind(final Object msg, final Map<String, Value> values,
      final QueryContext qc) throws QueryException {

    final Expr[] args = new Expr[function.arity()];
    if(msg != null) values.put("message", msg instanceof byte[] ?
      B64.get((byte[]) msg) : Str.get((String) msg));

    for(final WebParam param : headerParams) {
      bind(param.var, args, values.get(param.name), qc, "Value of \"" + param.name + '"');
    }
    if(message != null) {
      bind(message.var, args, values.get(message.name), qc, "Message");
    }
    return args;
  }

  /**
   * Checks if an WebSocket request matches this annotation and path.
   * @param definition annotation definition (can be {@code null})
   * @param pth path to compare to (can be {@code null})
   * @return boolean result of check
   */
  public boolean matches(final Annotation definition, final WsPath pth) {
    for(final Ann ann : function.anns) {
      if((definition == null || ann.definition == definition) &&
          (pth == null || path.compareTo(pth) == 0)) return true;
    }
    return false;
  }

  @Override
  public int compareTo(final WebFunction func) {
    return func instanceof WsFunction ? path.compareTo(((WsFunction) func).path) : 1;
  }

  /**
   * Creates an exception with the specified message.
   * @param msg message
   * @param ext error extension
   * @return exception
   */
  @Override
  public QueryException error(final String msg, final Object... ext) {
    return error(function.info, msg, ext);
  }

  // PRIVATE METHODS ==============================================================================

  /**
   * Creates an exception with the specific message.
   * @param ii input info
   * @param msg error message
   * @param ext error extension
   * @return exception
   */
  private static QueryException error(final InputInfo ii, final String msg, final Object... ext) {
    return BASEX_WS_X.get(ii, Util.info(msg, ext));
  }
}
