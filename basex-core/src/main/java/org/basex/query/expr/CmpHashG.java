package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class CmpHashG extends CmpG {
  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   * @param info input info
   */
  CmpHashG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(expr1, expr2, op, coll, sc, info);
  }

  /**
   * {@inheritDoc}
   * Overwrites the original comparator.
   */
  @Override
  Bln compare(final Iter iter1, final Iter iter2, final long is1, final long is2,
      final QueryContext qc) throws QueryException {

    // check if iterator is based on value with more than one item
    final Value value2 = iter2.iterValue();
    if(value2 != null && value2.size() > 1) {
      // retrieve cache (first call: initialize it)
      final CmpCache cache = qc.threads.get(this).get();

      // check if caching is enabled
      if(cache.active(value2, iter2)) {
        final HashItemSet set = cache.set;
        Iter ir2 = cache.iter;

        // loop through input items
        for(Item item1; (item1 = qc.next(iter1)) != null;) {
          // check if item has already been cached
          if(set.contains(item1, info)) {
            cache.hits++;
            return Bln.TRUE;
          }

          // cache remaining items (stop after first hit)
          if(ir2 != null) {
            for(Item item2; (item2 = qc.next(ir2)) != null;) {
              set.add(item2, info);
              if(set.contains(item1, info)) return Bln.TRUE;
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
    return copyType(new CmpHashG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info));
  }

  @Override
  public String description() {
    return "hashed " + super.description();
  }
}
