package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class CmpHashG extends CmpG {
  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param info input info (can be {@code null})
   */
  CmpHashG(final Expr expr1, final Expr expr2, final OpG op, final InputInfo info) {
    super(info, expr1, expr2, op);
  }

  /**
   * {@inheritDoc}
   * Overwrites the original comparator.
   */
  @Override
  Bln compare(final Iter iter1, final Iter iter2, final long is1, final long is2,
      final QueryContext qc) throws QueryException {

    // check if iterator is based on value with more than one item
    if(iter2.valueIter() && iter2.size() > 1) {
      // retrieve cache (first call: initialize it)
      final CmpCache cache = qc.threads.get(this, info).get();

      // check if caching is enabled
      if(cache.active(iter2.value(qc, null), iter2)) {
        final HashItemSet set = cache.set;
        Iter ir2 = cache.iter;

        // loop through input items
        for(Item item1; (item1 = qc.next(iter1)) != null;) {
          // check if item has already been cached
          if(set.contains(item1)) {
            cache.hits++;
            return Bln.TRUE;
          }

          // cache remaining items (stop after first hit)
          if(ir2 != null) {
            for(Item item2; (item2 = qc.next(ir2)) != null;) {
              set.add(item2);
              if(set.contains(item1)) return Bln.TRUE;
            }
            // iterator is exhausted, all items are cached
            cache.iter = null;
            ir2 = null;
          }
        }
        return Bln.FALSE;
      }
    }
    return super.compare(iter1, iter2, is1, is2, qc);
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return copyType(new CmpHashG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, info));
  }

  @Override
  public String description() {
    return "hashed " + super.description();
  }
}
