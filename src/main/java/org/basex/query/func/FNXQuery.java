package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.util.*;

/**
 * XQuery functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class FNXQuery extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNXQuery(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _XQUERY_EVAL:   return eval(ctx).iter();
      case _XQUERY_INVOKE: return run(ctx).iter();
      case _XQUERY_TYPE:   return value(ctx).iter();
      default:             return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _XQUERY_EVAL:   return eval(ctx);
      case _XQUERY_INVOKE: return run(ctx);
      case _XQUERY_TYPE:   return comp(ctx).value(ctx);
      default:             return super.value(ctx);
    }
  }

  @Override
  Expr comp(final QueryContext ctx) throws QueryException {
    if(sig == Function._XQUERY_TYPE) {
      FNInfo.dump(Util.inf("{ type: %, size: % }", expr[0].type(), expr[0].size()),
          Token.token(expr[0].toString()), ctx);
      return expr[0];
    }
    return this;
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return resulting value
   * @throws QueryException query exception
   */
  private static Value eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {

    final QueryContext qc = new QueryContext(ctx.context);
    qc.parse(string(qu));
    qc.compile();
    return qc.value();
  }

  /**
   * Performs the run function.
   * @param ctx query context
   * @return resulting value
   * @throws QueryException query exception
   */
  private Value run(final QueryContext ctx) throws QueryException {
    checkCreate(ctx);
    final String path = string(checkStr(expr[0], ctx));
    final IO io = IO.get(path);
    if(!io.exists()) FILE_IO.thrw(info, path);
    try {
      return eval(ctx, io.read());
    } catch(final IOException ex) {
      throw IOERR.thrw(info, ex);
    }
  }

  /**
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (can be {@code null})
   * @param ctx query context
   */
  static void dump(final long min, final byte[] msg, final QueryContext ctx) {
    Performance.gc(2);
    final long max = Performance.memory();
    final long mb = Math.max(0, max - min);
    FNInfo.dump(token(Performance.format(mb)), msg, ctx);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.NDT && oneOf(sig, _XQUERY_EVAL, _XQUERY_INVOKE) || super.uses(u);
  }
}
