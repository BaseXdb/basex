package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnStringToCodepoints extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    final byte[] token = value.string(info);
    final int tl = token.length;
    if(tl == 0) return Empty.ITER;

    if(value.ascii(info)) {
      return new BasicIter<Int>(tl) {
        @Override
        public Int get(final long i) {
          return Int.get(token[(int) i]);
        }
        @Override
        public Value value(final QueryContext q, final Expr expr) throws QueryException {
          return IntSeq.get(value.longCodepoints(info));
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Int next() {
        if(t == tl) return null;
        final int s = t;
        t += cl(token, s);
        return Int.get(cp(token, s));
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return IntSeq.get(toZeroStr(arg(0), qc).longCodepoints(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = arg(0);
    return value.seqType().zero() ? value : this;
  }
}
