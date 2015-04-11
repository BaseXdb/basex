package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FnInsertBefore extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    return new Iter() {
      final Iter iter = exprs[0].iter(qc);
      final long pos = Math.max(1, toLong(exprs[1], qc));
      final Iter ins = exprs[2].iter(qc);
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        while(!last) {
          final boolean sub = p == 0 || --p == 0;
          final Item i = (sub ? ins : iter).next();
          if(i != null) return i;
          if(sub) --p;
          else last = true;
        }
        return p > 0 ? ins.next() : null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value val = exprs[0].value(qc);
    final long pos = toLong(exprs[1], qc);
    final Value sub = exprs[2].value(qc);

    final long n = val.size(), k = sub.size();
    if(n == 0) return sub;
    if(k == 0) return val;

    final long p = Math.min(Math.max(0, pos - 1), n);
    if(n > 1) return ((Seq) val).insertBefore(p, sub);

    final Item v = (Item) val;
    if(k > 1) return ((Seq) sub).insert(p == 0 ? k : 0, v);

    final Item s = (Item) sub;
    return p == 0 ? Seq.get(s, v) : Seq.get(v, s);
  }
}
