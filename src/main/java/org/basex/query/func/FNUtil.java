package org.basex.query.func;

import static org.basex.query.util.Err.*;
import static org.basex.util.Token.*;
import java.io.IOException;
import org.basex.io.IO;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.Dbl;
import org.basex.query.item.Item;
import org.basex.query.item.Type;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.util.InputInfo;
import org.basex.util.Performance;

/**
 * Project specific functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNUtil extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNUtil(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case EVAL: return eval(ctx);
      case RUN:  return run(ctx);
      default:   return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case MB: return mb(ctx);
      case MS: return ms(ctx);
      default: return super.item(ctx, ii);
    }
  }

  /**
   * Performs the eval function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx) throws QueryException {
    return eval(ctx, checkEStr(expr[0], ctx));
  }

  /**
   * Performs the query function.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter run(final QueryContext ctx) throws QueryException {
    final IO io = checkIO(expr[0], ctx);
    try {
      return eval(ctx, io.content());
    } catch(final IOException ex) {
      NODOC.thrw(input, ex.toString());
      return null;
    }
  }

  /**
   * Evaluates the specified string.
   * @param ctx query context
   * @param qu query string
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter eval(final QueryContext ctx, final byte[] qu)
      throws QueryException {
    final QueryContext qt = new QueryContext(ctx.context);
    qt.parse(string(qu));
    qt.compile();
    return ItemIter.get(qt.iter());
  }

  /**
   * Measures the memory consumption for the specified expression in MB.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Dbl mb(final QueryContext ctx) throws QueryException {
    // check caching flag
    final boolean c = expr.length == 2 &&
      checkType(expr[1].item(ctx, input), Type.BLN).bool(input);

    // measure initial memory consumption
    Performance.gc(3);
    final long l = Performance.mem();

    // create (and, optionally, cache) result value
    Iter ir = expr[0].iter(ctx);
    final Value v = (c ? ItemIter.get(ir) : ir).finish();

    // measure resulting memory consumption
    Performance.gc(3);
    final double d = Performance.mem() - l;

    // loop through all results to avoid premature result disposal
    ir = v.iter();
    while(ir.next() != null);

    // return memory consumption in megabytes
    return Dbl.get(Math.max(0, d) / 1024 / 1024d);
  }

  /**
   * Measures the execution time for the specified expression in milliseconds.
   * @param ctx query context
   * @return time in milliseconds
   * @throws QueryException query exception
   */
  private Dbl ms(final QueryContext ctx) throws QueryException {
    // check caching flag
    final boolean c = expr.length == 2 &&
      checkType(expr[1].item(ctx, input), Type.BLN).bool(input);

    // create timer
    final Performance p = new Performance();

    // iterate (and, optionally, cache) results
    final Iter ir = expr[0].iter(ctx);
    if(c) {
      ItemIter.get(ir);
    } else {
      while(ir.next() != null);
    }

    // return measured time in milliseconds
    return Dbl.get(p.getTime() / 10000 / 100d);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.CTX || super.uses(u);
  }
}
