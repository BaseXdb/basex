package org.basex.http.ws;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import org.basex.core.*;
import org.basex.http.web.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * This class represents a single WebSocket function.
 *
 * @author BaseX Team, BSD License
 * @author Johannes Finckh
 */
public final class WsFunction extends WebFunction {
  /** Path of the function. */
  public WebPath path;
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
  public boolean parseAnnotations(final MainOptions mopts) throws QueryException {
    final boolean[] declared = new boolean[function.arity()];
    // counter for annotations that should occur only once
    boolean found = false;
    AnnList starts = AnnList.EMPTY;

    for(final Ann ann : function.anns) {
      final Annotation def = ann.definition;
      if(def == null || !eq(def.name.uri(), QueryText.WS_URI)) continue;

      found = true;
      final Value value = ann.value();
      switch(def) {
        case _WS_CLOSE:
        case _WS_CONNECT:
          path = parsePath(value, ann, declared);
          starts = starts.attach(ann);
          break;
        case _WS_ERROR:
        case _WS_MESSAGE:
          final QNm msg = checkVariable(toString(value.itemAt(1)), declared);
          message = new WebParam(msg, "message", null);
          path = parsePath(value, ann, declared);
          starts = starts.attach(ann);
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
   * @param pth concrete connection path
   * @param qc query context
   * @return arguments
   * @throws QueryException query exception
   */
  Expr[] bind(final Object msg, final String pth, final QueryContext qc) throws QueryException {
    final Expr[] args = new Expr[function.arity()];
    // bind variables from path template (resolved once, at the handshake)
    final QNmMap<String> qnames = path.values(pth);
    for(final QNm qname : qnames) {
      final QNm qnm = new QNm(qname.string(), function.sc);
      if(function.sc.elemNS != null && eq(qnm.uri(), function.sc.elemNS)) qnm.uri(EMPTY);
      bind(qnm, args, Atm.get(qnames.get(qname)), qc, "Path segment");
    }
    if(message != null) {
      Value value = null;
      if(msg instanceof final byte[] bytes) value = B64.get(bytes);
      else if(msg != null) value = Str.get((String) msg);
      bind(message.var(), args, value, qc, "Message");
    }
    return args;
  }

  /**
   * Checks if a WebSocket request matches this annotation and path.
   * @param definition annotation definition (can be {@code null})
   * @param pth concrete connection path (can be {@code null})
   * @return boolean result of check
   */
  public boolean matches(final Annotation definition, final String pth) {
    if(pth != null && !path.matches(pth)) return false;
    if(definition == null) return true;
    for(final Ann ann : function.anns) {
      if(ann.definition == definition) return true;
    }
    return false;
  }

  @Override
  public int compareTo(final WebFunction func) {
    return func instanceof final WsFunction ws ? path.compareTo(ws.path) : 1;
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
   * Parses the path template of an annotation and registers its variables.
   * @param value annotation value
   * @param ann annotation
   * @param declared variable declaration flags
   * @return path template
   * @throws QueryException query exception
   */
  private WebPath parsePath(final Value value, final Ann ann, final boolean[] declared)
      throws QueryException {
    final WebPath pth;
    try {
      pth = new WebPath(toString(value.itemAt(0)), ann.info, BASEX_WS_X);
    } catch(final IllegalArgumentException ex) {
      throw error(ann.info, ex.getMessage());
    }
    for(final QNm name : pth.varNames()) checkVariable(name, declared);
    return pth;
  }

  /**
   * Creates an exception with the specific message.
   * @param info input info (can be {@code null})
   * @param msg error message
   * @param ext error extension
   * @return exception
   */
  private static QueryException error(final InputInfo info, final String msg, final Object... ext) {
    return BASEX_WS_X.get(info, Util.info(msg, ext));
  }
}
