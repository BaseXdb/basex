package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Iterative filter expression without numeric predicates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class IterFilter extends Filter {
  /**
   * Constructor.
   * @param info input info (can be {@code null})
   * @param root root expression
   * @param preds predicate expressions
   */
  IterFilter(final InputInfo info, final Expr root, final Expr... preds) {
    super(info, root, preds);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value range = range(iter);
    if(range != null) return range.iter();

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        final QueryContext q = qc;
        final Iter ir = iter;
        for(Item item; (item = q.next(ir)) != null;) {
          if(test(item, q)) return item;
        }
        return null;
      }
    };
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    final Iter iter = root.iter(qc);
    final Value range = range(iter);
    if(range != null) return range;

    final ValueBuilder vb = new ValueBuilder(qc);
    for(Item item; (item = qc.next(iter)) != null;) {
      if(test(item, qc)) vb.add(item);
    }
    return vb.value(this);
  }

  /**
   * Returns the subrange of a range that is filtered by an integer range comparison.
   * @param iter iterator of the root expression
   * @return subrange, or {@code null} if the root is no range or the predicate does not match
   */
  private Value range(final Iter iter) {
    if(exprs.length == 1 && exprs[0] instanceof final CmpIR cmp &&
        cmp.expr instanceof ContextValue && iter.eagerValue() instanceof final RangeSeq rs) {
      // (1 to $n)[. >= 5] → 5 to $n
      final long min = Math.max(rs.min(), cmp.min), max = Math.min(rs.max(), cmp.max);
      final boolean asc = rs.ascending();
      return min > max ? Empty.VALUE : RangeSeq.get(asc ? min : max, max - min + 1, asc);
    }
    return null;
  }

  @Override
  public IterFilter copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new IterFilter(info, root.copy(cc, vm), copyAll(cc, vm, exprs)));
  }
}
