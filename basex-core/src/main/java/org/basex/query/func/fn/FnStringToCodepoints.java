package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnStringToCodepoints extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final int tl = value.length;

    if(ascii(value)) {
      return new BasicIter<Int>(tl) {
        @Override
        public Int get(final long i) {
          return Int.get(value[(int) i]);
        }
        @Override
        public Value value(final QueryContext q, final Expr expr) throws QueryException {
          final LongList list = new LongList(Seq.initialCapacity(size));
          for(final byte b : value) list.add(b);
          return IntSeq.get(list);
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Int next() {
        if(t == tl) return null;
        final int cp = cp(value, t);
        t += cl(value, t);
        return Int.get(cp);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final int[] cps = cps(toZeroToken(arg(0), qc));
    final LongList list = new LongList(cps.length);
    for(final int cp : cps) list.add(cp);
    return IntSeq.get(list);
  }
}
