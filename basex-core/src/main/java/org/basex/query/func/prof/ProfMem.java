package org.basex.query.func.prof;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.func.fn.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class ProfMem extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
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
   * Dumps the memory consumption.
   * @param min initial memory usage
   * @param msg message (may be {@code null})
   * @param qc query context
   */
  private static void dump(final long min, final byte[] msg, final QueryContext qc) {
    Performance.gc(2);
    final long max = Performance.memory();
    final long mb = Math.max(0, max - min);
    FnTrace.dump(token(Performance.format(mb)), msg, qc);
  }
}
