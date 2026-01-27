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
 * @author BaseX Team, BSD License
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
      return new BasicIter<Itr>(tl) {
        @Override
        public Itr get(final long i) {
          return Itr.get(token[(int) i]);
        }
        @Override
        public Value value(final QueryContext q, final Expr expr) throws QueryException {
          return IntSeq.get(value.codepoints(info));
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Itr next() {
        if(t == tl) return null;
        final int s = t;
        t += cl(token, s);
        return Itr.get(cp(token, s));
      }
      @Override
      public Value value(final QueryContext q, final Expr expr) throws QueryException {
        return IntSeq.get(value.codepoints(info));
      }
    };
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = arg(0);
    return value.seqType().zero() ? value : this;
  }
}
