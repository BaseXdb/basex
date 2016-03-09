package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.CmpV.OpV;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;

/**
 * Numeric functions.
 *
 * @author BaseX Team 2005-16, BSD License
 * @author Christian Gruen
 */
abstract class Num extends StandardFunc {
  /**
   * Rounds values.
   * @param qc query context
   * @param even half-to-even flag
   * @return number
   * @throws QueryException query exception
   */
  ANum round(final QueryContext qc, final boolean even) throws QueryException {
    final ANum num = toNumber(exprs[0], qc);
    final long p = exprs.length == 1 ? 0 : Math.max(Integer.MIN_VALUE, toLong(exprs[1], qc));
    return num == null ? null : p > Integer.MAX_VALUE ? num : num.round((int) p, even);
  }

  /**
   * Returns a minimum or maximum item.
   * @param cmp comparator
   * @param qc query context
   * @return resulting item
   * @throws QueryException query exception
   */
  Item minmax(final OpV cmp, final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);

    final Iter iter = exprs[0].atomIter(qc, info);
    Item curr = iter.next();
    if(curr == null) return null;

    // check if first item is comparable
    cmp.eval(curr, curr, coll, sc, info);

    // strings
    if(curr instanceof AStr) {
      for(Item it; (it = iter.next()) != null;) {
        if(!(it instanceof AStr)) throw MINMAX_X_X_X.get(info, curr.type, it.type, it);
        final Type rt = curr.type, ri = it.type;
        if(cmp.eval(curr, it, coll, sc, info)) curr = it;
        if(rt != ri && curr.type == URI) curr = STR.cast(curr, qc, sc, info);
      }
      return curr;
    }
    // dates, durations, booleans, binary values
    if(curr instanceof ADate || curr instanceof Dur || curr instanceof Bin || curr.type == BLN) {
      for(Item it; (it = iter.next()) != null;) {
        if(curr.type != it.type) throw MINMAX_X_X_X.get(info, curr.type, it.type, it);
        if(cmp.eval(curr, it, coll, sc, info)) curr = it;
      }
      return curr;
    }
    // numbers
    if(curr.type.isUntyped()) curr = DBL.cast(curr, qc, sc, info);
    for(Item it; (it = iter.next()) != null;) {
      final Type type = numType(curr, it);
      if(cmp.eval(curr, it, coll, sc, info) || Double.isNaN(it.dbl(info))) curr = it;
      if(type != null) curr = (Item) type.cast(curr, qc, sc, info);
    }
    return curr;
  }

  /**
   * Returns the new target type, or {@code null} if conversion is not necessary.
   * @param curr old item
   * @param it new item
   * @return result (or {@code null})
   * @throws QueryException query exception
   */
  private AtomType numType(final Item curr, final Item it) throws QueryException {
    final Type ti = it.type;
    if(ti.isUntyped()) return DBL;
    final Type tc = curr.type;
    if(!(it instanceof ANum)) throw MINMAX_X_X_X.get(info, tc, ti, it);
    return tc == ti ? null :
           tc == DBL || ti == DBL ? DBL :
           tc == FLT || ti == FLT ? FLT :
           null;
  }
}
