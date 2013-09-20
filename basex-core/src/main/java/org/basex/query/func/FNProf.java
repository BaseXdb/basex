package org.basex.query.func;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Profiling functions.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Christian Gruen
 */
public final class FNProf extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNProf(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _PROF_MEM:  return mem(ctx);
      case _PROF_TIME: return time(ctx);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _PROF_SLEEP:      return sleep(ctx);
      case _PROF_CURRENT_MS: return Int.get(System.currentTimeMillis());
      case _PROF_CURRENT_NS: return Int.get(System.nanoTime());
      case _PROF_DUMP:       return dump(ctx);
      case _PROF_HUMAN:      return human(ctx);
      case _PROF_VOID:       return voidd(ctx);
      default:               return super.item(ctx, ii);
    }
  }

  /**
   * Measures the memory consumption for the specified expression in MB.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Iter mem(final QueryContext ctx) throws QueryException {
    // measure initial memory consumption
    Performance.gc(3);
    final long min = Performance.memory();

    // optional message
    final byte[] msg = expr.length > 2 ? checkStr(expr[2], ctx) : null;

    // check caching flag
    if(expr.length > 1 && checkBln(expr[1], ctx)) {
      final Value v = ctx.value(expr[0]).cache().value();
      dump(min, msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) dump(min, msg, ctx);
        return it;
      }
    };
  }

  /**
   * Dumps the items of a sequence.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item dump(final QueryContext ctx) throws QueryException {
    final Iter ir = expr[0].iter(ctx);
    final byte[] s = expr.length > 1 ? checkStr(expr[1], ctx) : null;
    for(Item it; (it = ir.next()) != null;) {
      try {
        FNInfo.dump(it.serialize().toArray(), s, ctx);
      } catch(final QueryIOException ex) {
        throw ex.getCause(info);
      }
    }
    return null;
  }

  /**
   * Materializes and swallows the input.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item voidd(final QueryContext ctx) throws QueryException {
    final Iter ir = expr[0].iter(ctx);
    for(Item it; (it = ir.next()) != null;) it.materialize(info);
    return null;
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

  /**
   * Returns a human-readable version of the specified integer.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item human(final QueryContext ctx) throws QueryException {
    return Str.get(Performance.format(checkItr(expr[0], ctx), true));
  }

  /**
   * Measures the execution time for the specified expression in milliseconds.
   * @param ctx query context
   * @return time in milliseconds
   * @throws QueryException query exception
   */
  private Iter time(final QueryContext ctx) throws QueryException {
    // create timer
    final Performance p = new Performance();

    // optional message
    final byte[] msg = expr.length > 2 ? checkStr(expr[2], ctx) : null;

    // check caching flag
    if(expr.length > 1 && checkBln(expr[1], ctx)) {
      final Value v = ctx.value(expr[0]).cache().value();
      FNInfo.dump(token(p.getTime()), msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) FNInfo.dump(token(p.getTime()), msg, ctx);
        return it;
      }
    };
  }

  /**
   * Sleeps for the specified number of milliseconds.
   * @param ctx query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item sleep(final QueryContext ctx) throws QueryException {
    Performance.sleep(checkItr(expr[0], ctx));
    return null;
  }
}
