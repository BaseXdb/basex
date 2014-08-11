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
public final class FNProf extends BuiltinFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNProf(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _PROF_MEM:  return mem(qc);
      case _PROF_TIME: return time(qc);
      default:         return super.iter(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _PROF_SLEEP:      return sleep(qc);
      case _PROF_CURRENT_MS: return Int.get(System.currentTimeMillis());
      case _PROF_CURRENT_NS: return Int.get(System.nanoTime());
      case _PROF_DUMP:       return dump(qc);
      case _PROF_HUMAN:      return human(qc);
      case _PROF_VOID:       return voidd(qc);
      default:               return super.item(qc, ii);
    }
  }

  /**
   * Measures the memory consumption for the specified expression in MB.
   * @param qc query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Iter mem(final QueryContext qc) throws QueryException {
    // measure initial memory consumption
    Performance.gc(3);
    final long min = Performance.memory();

    // optional message
    final byte[] msg = exprs.length > 2 ? toToken(exprs[2], qc) : null;

    // check caching flag
    if(exprs.length > 1 && toBoolean(exprs[1], qc)) {
      final Value v = qc.value(exprs[0]).cache().value();
      dump(min, msg, qc);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) dump(min, msg, qc);
        return it;
      }
    };
  }

  /**
   * Dumps the items of a sequence.
   * @param qc query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item dump(final QueryContext qc) throws QueryException {
    final Iter ir = exprs[0].iter(qc);
    final byte[] label = exprs.length > 1 ? toToken(exprs[1], qc) : null;
    boolean empty = true;
    for(Item it; (it = ir.next()) != null;) {
      FNInfo.dump(it, label, info, qc);
      empty = false;
    }
    if(empty) FNInfo.dump(null, label, info, qc);
    return null;
  }

  /**
   * Materializes and swallows the input.
   * @param qc query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item voidd(final QueryContext qc) throws QueryException {
    final Iter ir = exprs[0].iter(qc);
    for(Item it; (it = ir.next()) != null;) it.materialize(info);
    return null;
  }

  /**
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (can be {@code null})
   * @param qc query context
   */
  private static void dump(final long min, final byte[] msg, final QueryContext qc) {
    Performance.gc(2);
    final long max = Performance.memory();
    final long mb = Math.max(0, max - min);
    FNInfo.dump(token(Performance.format(mb)), msg, qc);
  }

  /**
   * Returns a human-readable version of the specified integer.
   * @param qc query context
   * @return memory consumption
   * @throws QueryException query exception
   */
  private Item human(final QueryContext qc) throws QueryException {
    return Str.get(Performance.format(toLong(exprs[0], qc), true));
  }

  /**
   * Measures the execution time for the specified expression in milliseconds.
   * @param qc query context
   * @return time in milliseconds
   * @throws QueryException query exception
   */
  private Iter time(final QueryContext qc) throws QueryException {
    // create timer
    final Performance p = new Performance();

    // optional message
    final byte[] msg = exprs.length > 2 ? toToken(exprs[2], qc) : null;

    // check caching flag
    if(exprs.length > 1 && toBoolean(exprs[1], qc)) {
      final Value v = qc.value(exprs[0]).cache().value();
      FNInfo.dump(token(p.getTime()), msg, qc);
      return v.iter();
    }

    return new Iter() {
      final Iter ir = exprs[0].iter(qc);
      @Override
      public Item next() throws QueryException {
        final Item it = ir.next();
        if(it == null) FNInfo.dump(token(p.getTime()), msg, qc);
        return it;
      }
    };
  }

  /**
   * Sleeps for the specified number of milliseconds.
   * @param qc query context
   * @return {@code null}
   * @throws QueryException query exception
   */
  private Item sleep(final QueryContext qc) throws QueryException {
    Performance.sleep(toLong(exprs[0], qc));
    return null;
  }
}
