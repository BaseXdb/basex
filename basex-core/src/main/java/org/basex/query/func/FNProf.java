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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FNProf extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNProf(final StaticContext sctx, final InputInfo info, final Function func,
      final Expr... args) {
    super(sctx, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case _PROF_MEM:  return mem(ctx);
      case _PROF_TIME: return time(ctx);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(func) {
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
    final byte[] msg = exprs.length > 2 ? checkStr(exprs[2], ctx) : null;

    // check caching flag
    if(exprs.length > 1 && checkBln(exprs[1], ctx)) {
      final Value v = ctx.value(exprs[0]).cache().value();
      dump(min, msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = exprs[0].iter(ctx);
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
    final Iter ir = exprs[0].iter(ctx);
    final byte[] label = exprs.length > 1 ? checkStr(exprs[1], ctx) : null;
    boolean empty = true;
    for(Item it; (it = ir.next()) != null;) {
      FNInfo.dump(it, label, info, ctx);
      empty = false;
    }
    if(empty) FNInfo.dump(null, label, info, ctx);
    return null;
  }

  /**
   * Materializes and swallows the input.
   * @param ctx query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item voidd(final QueryContext ctx) throws QueryException {
    final Iter ir = exprs[0].iter(ctx);
    for(Item it; (it = ir.next()) != null;) it.materialize(info);
    return null;
  }

  /**
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (can be {@code null})
   * @param ctx query context
   */
  private static void dump(final long min, final byte[] msg, final QueryContext ctx) {
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
    return Str.get(Performance.format(checkItr(exprs[0], ctx), true));
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
    final byte[] msg = exprs.length > 2 ? checkStr(exprs[2], ctx) : null;

    // check caching flag
    if(exprs.length > 1 && checkBln(exprs[1], ctx)) {
      final Value v = ctx.value(exprs[0]).cache().value();
      FNInfo.dump(token(p.getTime()), msg, ctx);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = exprs[0].iter(ctx);
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
    Performance.sleep(checkItr(exprs[0], ctx));
    return null;
  }
}
