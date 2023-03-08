package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnRemove extends StandardFunc {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter input = arg(0).iter(qc);
    final LongList pos = positions(qc);

    // value-based iterator
    if(input.valueIter() || pos.size() > 1) return value(input.value(qc, null), pos, qc).iter();

    final long size = input.size();
    return new Iter() {
      final long p = pos.get(0);
      long c;

      @Override
      public Item next() throws QueryException {
        return c++ != p || input.next() != null ? input.next() : null;
      }
      @Override
      public Item get(final long i) throws QueryException {
        return input.get(i + 1 < p ? i : i + 1);
      }
      @Override
      public long size() {
        return Math.max(-1, size - 1);
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc);
    final LongList pos = positions(qc);
    return value(input, pos, qc);
  }

  /**
   * Returns the result value.
   * @param value original value
   * @param list positions of the items to remove (sorted, duplicate-free)
   * @param qc query context
   * @return resulting value
   */
  private static Value value(final Value value, final LongList list, final QueryContext qc) {
    Value v = value;
    for(int l = list.size() - 1; l >= 0 && !v.isEmpty(); l--) {
      final long pos = list.get(l), size = v.size();
      if(pos == 0) {
        // remove first item
        v = v.subsequence(1, size - 1, qc);
      } else if(pos == size - 1) {
        // remove last item
        v = v.subsequence(0, size - 1, qc);
      } else if(pos > 0 && pos < size) {
        // remove item at supplied position
        v = ((Seq) v).remove(pos, qc);
      }
    }
    return v;
  }

  /**
   * Returns sorted and duplicate-free positions.
   * @param qc query context
   * @return positions
   * @throws QueryException query exception
   */
  private LongList positions(final QueryContext qc) throws QueryException {
    final LongList pos = new LongList();
    final Iter iter = arg(1).iter(qc);
    for(Item item; (item = qc.next(iter)) != null;) pos.add(toLong(item) - 1);
    return pos.ddo();
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // ignore standard limitation for large values to speed up evaluation of result
    if(allAreValues(false)) return value(cc.qc);

    final Expr input = arg(0), pos = arg(1);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    long sz = -1;
    if(pos instanceof Item && pos.size() == 1) {
      // position is static...
      final long p = toLong(pos, cc.qc);
      // return all items
      final long size = input.size();
      if(p < 1 || size > 0 && p > size) return input;
      // skip first item
      if(p == 1) return cc.function(Function.TAIL, info, input);
      // skip last item
      if(p == size) return cc.function(Function.TRUNK, info, input);
      // decrement result size
      sz--;
    }

    exprType.assign(st.union(Occ.ZERO), sz).data(input);
    return this;
  }

  @Override
  public boolean ddo() {
    return arg(0).ddo();
  }
}
