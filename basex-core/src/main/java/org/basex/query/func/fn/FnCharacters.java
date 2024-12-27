package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import java.util.*;

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
public final class FnCharacters extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    final byte[] token = toToken(value);
    final int tl = token.length;
    if(tl == 0) return Empty.ITER;

    if(value.ascii(info)) {
      return new BasicIter<Str>(tl) {
        @Override
        public Str get(final long i) {
          return Str.get(new byte[] { token[(int) i] });
        }
        @Override
        public Value value(final QueryContext q, final Expr expr) throws QueryException {
          return StrSeq.get(value.characters(info));
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Str next() {
        if(t == tl) return null;
        final int s = t, e = s + cl(token, s);
        t = e;
        return Str.get(Arrays.copyOfRange(token, s, e));
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    return StrSeq.get(value.characters(info));
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr value = arg(0);
    return value.seqType().zero() ? value : this;
  }
}
