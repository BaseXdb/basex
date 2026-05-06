package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General hash-based comparison.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CmpHashG extends CmpG {
  /** Right-hand operand is deterministic and closed: cache is content-stable across calls. */
  private final boolean stable;

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param info input info (can be {@code null})
   */
  CmpHashG(final Expr expr1, final Expr expr2, final CmpOp op, final InputInfo info) {
    super(info, expr1, expr2, op);
    stable = expr2.isSimple() && !expr2.hasFreeVars();
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    final Iter iter1 = exprs[0].atomIter(qc, info);
    final long size1 = iter1.size();
    if(size1 == 0) return false;

    // stable right-hand operand: probe against populated cache without re-evaluating expr2
    final CmpCache cache = qc.threads.get(this, info).get();
    if(stable && cache.value != null) return probe(iter1, cache, qc);

    // dynamic right-hand operand: evaluate, consult cache, fall back if not eligible
    final Iter iter2 = exprs[1].atomIter(qc, info);
    final long size2 = iter2.size();
    if(size2 == 0) return false;
    // check if iterator is based on value with more than one item, check if caching is enabled
    if(iter2.valueIter() && size2 > 1 && cache.active(iter2.value(qc, null), iter2)) {
      return probe(iter1, cache, qc);
    }
    return super.compare(iter1, iter2, size1, size2, qc);
  }

  /**
   * Probes items of the left-hand operand against the cached hash set, lazily extending the set
   * from the cached right-hand iterator on misses.
   * @param iter1 left-hand iterator
   * @param cache active cache (set and iter must be initialized)
   * @param qc query context
   * @return {@code true} on first hit, {@code false} if no item matches
   * @throws QueryException query exception
   */
  private static boolean probe(final Iter iter1, final CmpCache cache, final QueryContext qc)
      throws QueryException {
    final HashItemSet set = cache.set;
    Iter ir2 = cache.iter;

    // loop through input items
    for(Item item1; (item1 = qc.next(iter1)) != null;) {
      // check if item has already been cached
      if(set.contains(item1)) {
        cache.hit = true;
        return true;
      }

      // cache remaining items (stop after first hit)
      if(ir2 != null) {
        for(Item item2; (item2 = qc.next(ir2)) != null;) {
          set.add(item2);
          if(set.contains(item1)) {
            cache.hit = true;
            return true;
          }
        }
        // iterator exhausted, all items are cached
        cache.iter = null;
        ir2 = null;
      }
    }
    return false;
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjectMap<Var> vm) {
    return copyType(new CmpHashG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public String description() {
    return "hashed " + super.description();
  }
}
