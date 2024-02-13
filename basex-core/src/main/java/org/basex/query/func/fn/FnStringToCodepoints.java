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
          final LongList list = new LongList(Seq.initialCapacity(size));
          for(final byte b : token) list.add(b);
          return IntSeq.get(list.finish());
        }
      };
    }

    return new Iter() {
      int t;

      @Override
      public Int next() {
        if(t == tl) return null;
        final int cp = cp(token, t);
        t += cl(token, t);
        return Int.get(cp);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    final byte[] token = value.string(info);

    final LongList list = new LongList(value.length(info));
    if(value.ascii(info)) {
      for(final byte b : token) list.add(b);
    } else {
      final int tl = token.length;
      for(int t = 0; t < tl; t += cl(token, t)) list.add(cp(token, t));
    }
    return IntSeq.get(list.finish());
  }
}
