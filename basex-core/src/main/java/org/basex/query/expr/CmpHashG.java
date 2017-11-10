package org.basex.query.expr;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.hash.*;
import org.basex.query.value.item.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * General comparison.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class CmpHashG extends CmpG {
  /** Thread-safe caching. */
  private final ThreadLocal<CmpCache> caches = new ThreadLocal<>();

  /**
   * Constructor.
   * @param expr1 first expression
   * @param expr2 second expression
   * @param op operator
   * @param coll collation (can be {@code null})
   * @param sc static context
   * @param info input info
   */
  public CmpHashG(final Expr expr1, final Expr expr2, final OpG op, final Collation coll,
      final StaticContext sc, final InputInfo info) {
    super(expr1, expr2, op, coll, sc, info);
  }

  @Override
  public Expr optimize(final CompileContext cc) throws QueryException {
    // pre-evaluate values
    return allAreValues() ? cc.preEval(this) : this;
  }

  @Override
  public Bln item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter1 = exprs[0].atomIter(qc, info);
    if(iter1.size() == 0) return Bln.FALSE;

    // first call: initialize hash
    CmpCache cache = caches.get();
    if(cache == null) {
      cache = new CmpCache();
      caches.set(cache);
      final Iter ir2 = exprs[1].atomIter(qc, info);
      // no values: no need to cache
      if(ir2.size() == 0) return Bln.FALSE;
      cache.iter2 = ir2;
    }
    final HashItemSet set = cache.set;

    // loop through input
    for(Item it1; (it1 = iter1.next()) != null;) {
      qc.checkStop();
      // check if value has already been cached
      if(set.contains(it1, info)) return Bln.TRUE;

      // cache remaining values (stop after first hit)
      if(cache.iter2 != null) {
        for(Item it2; (it2 = cache.iter2.next()) != null;) {
          qc.checkStop();
          set.add(it2, ii);
          if(set.contains(it1, info)) return Bln.TRUE;
        }
        cache.iter2 = null;
      }
    }
    return Bln.FALSE;
  }

  @Override
  public CmpG copy(final CompileContext cc, final IntObjMap<Var> vm) {
    return new CmpHashG(exprs[0].copy(cc, vm), exprs[1].copy(cc, vm), op, coll, sc, info);
  }

  @Override
  public String description() {
    return "hashed '" + op + "' operator";
  }

  /**
   * Cached items.
   * @author BaseX Team 2005-17, BSD License
   * @author Christian Gruen
   */
  private static final class CmpCache {
    /** cached items. */
    final HashItemSet set = new HashItemSet(true);;
    /** Lazy iterator. */
    Iter iter2;
  }
}
