package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.CmpV.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public class FnMin extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return minmax(OpV.GT, qc);
  }

  /**
   * Returns a minimum or maximum item.
   * @param cmp comparator
   * @param qc query context
   * @return resulting item or {@code null}
   * @throws QueryException query exception
   */
  final Item minmax(final OpV cmp, final QueryContext qc) throws QueryException {
    final Collation coll = toCollation(1, qc);
    final Expr ex = exprs[0];
    Item it1 = value(cmp);
    if(it1 != null) return it1;

    if(ex instanceof Range) {
      final Value v = qc.value(ex);
      return v.isEmpty() ? null : v.itemAt(cmp == OpV.GT ? 0 : v.size() - 1);
    }

    final Iter iter = ex.atomIter(qc, info);
    it1 = iter.next();
    if(it1 == null) return null;

    // ensure that item is sortable
    final Type t1 = it1.type;
    if(!t1.isSortable()) throw CMP_X.get(info, t1);

    // strings and URIs
    if(it1 instanceof AStr) {
      for(Item it2; (it2 = iter.next()) != null;) {
        qc.checkStop();
        if(!(it2 instanceof AStr)) throw CMP_X_X_X.get(info, t1, it2.type, it2);
        final Type t2 = it2.type;
        if(cmp.eval(it1, it2, coll, sc, info)) it1 = it2;
        if(t1 != t2 && it1.type == AtomType.URI) it1 = STR.cast(it1, qc, sc, info);
      }
      return it1;
    }
    // booleans, dates, durations, binaries
    if(t1 == BLN || it1 instanceof ADate || it1 instanceof Dur || it1 instanceof Bin) {
      for(Item it; (it = iter.next()) != null;) {
        qc.checkStop();
        if(t1 != it.type) throw CMP_X_X_X.get(info, t1, it.type, it);
        if(cmp.eval(it1, it, coll, sc, info)) it1 = it;
      }
      return it1;
    }
    // numbers
    if(t1.isUntyped()) it1 = DBL.cast(it1, qc, sc, info);
    for(Item it2; (it2 = iter.next()) != null;) {
      qc.checkStop();
      final AtomType t = numType(it1, it2);
      if(cmp.eval(it1, it2, coll, sc, info) || Double.isNaN(it2.dbl(info))) it1 = it2;
      if(t != null) it1 = t.cast(it1, qc, sc, info);
    }
    return it1;
  }

  /**
   * Returns the new target type, or {@code null} if conversion is not necessary.
   * @param it1 old item
   * @param it2 new item
   * @return result (or {@code null})
   * @throws QueryException query exception
   */
  private AtomType numType(final Item it1, final Item it2) throws QueryException {
    final Type t2 = it2.type;
    if(t2.isUntyped()) return DBL;
    final Type t1 = it1.type;
    if(!(it2 instanceof ANum)) throw CMP_X_X_X.get(info, t1, t2, it2);
    return t1 == t2 ? null :
           t1 == DBL || t2 == DBL ? DBL :
           t1 == FLT || t2 == FLT ? FLT :
           null;
  }

  /**
   * Evaluate value arguments.
   * @param cmp comparator
   * @return smallest value or {@code null}
   */
  private Item value(final OpV cmp) {
    final Expr ex = exprs[0];
    if(ex instanceof Value && exprs.length < 2) {
      Item it = null;
      final Value v = (Value) ex;
      if(v instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) v;
        it = seq.itemAt((cmp == OpV.GT ^ seq.asc) ? seq.size() - 1 : 0);
      }
      if(ex instanceof SingletonSeq || ex instanceof Item) {
        it = v.itemAt(cmp == OpV.GT ? 0 : ex.size() - 1);
      }
      if(it != null) {
        final Type t = it.seqType().type;
        if(t.isNumber() || t.instanceOf(AtomType.STR)) return it;
      }
    }
    return null;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return optMinmax(OpV.GT);
  }

  /**
   * Optimizes a minimum or maximum item.
   * @param cmp comparator
   * @return optimized or original item
   */
  final Expr optMinmax(final OpV cmp) {
    final Expr ex = exprs[0];
    final SeqType st = ex.seqType();
    Type t = st.type;
    if(t.isSortable()) {
      if(t.isUntyped()) {
        t = AtomType.DBL;
      } else if(st.one() && exprs.length < 2) {
        return ex;
      }
      exprType.assign(t);
      final Item it = value(cmp);
      if(it != null) return it;
    }
    return optFirst();
  }
}
