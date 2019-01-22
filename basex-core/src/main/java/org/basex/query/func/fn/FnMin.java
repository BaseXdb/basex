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
 * @author BaseX Team 2005-19, BSD License
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
    final Expr expr = exprs[0];
    Item item1 = value(cmp);
    if(item1 != null) return item1;

    if(expr instanceof Range) {
      final Value value = expr.value(qc);
      return value.isEmpty() ? null : value.itemAt(cmp == OpV.GT ? 0 : value.size() - 1);
    }

    final Iter iter = expr.atomIter(qc, info);
    item1 = iter.next();
    if(item1 == null) return null;

    // ensure that item is sortable
    final Type type1 = item1.type;
    if(!type1.isSortable()) throw CMP_X.get(info, type1);

    // strings and URIs
    if(item1 instanceof AStr) {
      for(Item item2; (item2 = qc.next(iter)) != null;) {
        if(!(item2 instanceof AStr)) throw CMP_X_X_X.get(info, type1, item2.type, item2);
        final Type type2 = item2.type;
        if(cmp.eval(item1, item2, coll, sc, info)) item1 = item2;
        if(type1 != type2 && item1.type == AtomType.URI) item1 = STR.cast(item1, qc, sc, info);
      }
      return item1;
    }
    // booleans, dates, durations, binaries
    if(type1 == BLN || item1 instanceof ADate || item1 instanceof Dur || item1 instanceof Bin) {
      for(Item item; (item = qc.next(iter)) != null;) {
        if(type1 != item.type) throw CMP_X_X_X.get(info, type1, item.type, item);
        if(cmp.eval(item1, item, coll, sc, info)) item1 = item;
      }
      return item1;
    }
    // numbers
    if(type1.isUntyped()) item1 = DBL.cast(item1, qc, sc, info);
    for(Item item2; (item2 = qc.next(iter)) != null;) {
      final AtomType type = numType(item1, item2);
      if(cmp.eval(item1, item2, coll, sc, info) || Double.isNaN(item2.dbl(info))) item1 = item2;
      if(type != null) item1 = type.cast(item1, qc, sc, info);
    }
    return item1;
  }

  /**
   * Returns the new target type, or {@code null} if conversion is not necessary.
   * @param item1 first item
   * @param item2 second item
   * @return result (or {@code null})
   * @throws QueryException query exception
   */
  private AtomType numType(final Item item1, final Item item2) throws QueryException {
    final Type type2 = item2.type;
    if(type2.isUntyped()) return DBL;
    final Type type1 = item1.type;
    if(!(item2 instanceof ANum)) throw CMP_X_X_X.get(info, type1, type2, item2);
    return type1 == type2 ? null :
           type1 == DBL || type2 == DBL ? DBL :
           type1 == FLT || type2 == FLT ? FLT :
           null;
  }

  /**
   * Evaluate value arguments.
   * @param cmp comparator
   * @return smallest value or {@code null}
   */
  private Item value(final OpV cmp) {
    final Expr expr = exprs[0];
    if(expr instanceof Value && exprs.length < 2) {
      Item item = null;
      final Value value = (Value) expr;
      if(value instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) value;
        item = seq.itemAt((cmp == OpV.GT ^ seq.asc) ? seq.size() - 1 : 0);
      }
      if(expr instanceof SingletonSeq || expr instanceof Item) {
        item = value.itemAt(cmp == OpV.GT ? 0 : expr.size() - 1);
      }
      if(item != null) {
        final Type type = item.seqType().type;
        if(type.isNumber() || type.instanceOf(AtomType.STR)) return item;
      }
    }
    return null;
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    return optMinmax(OpV.GT);
  }

  /**
   * Optimizes a minimum or maximum item.
   * @param cmp comparator
   * @return optimized or original item
   */
  final Expr optMinmax(final OpV cmp) {
    final Expr expr = exprs[0];
    final SeqType st = expr.seqType();
    Type type = st.type;
    if(type.isSortable()) {
      if(type.isUntyped()) {
        type = AtomType.DBL;
      } else if(st.one() && exprs.length < 2) {
        return expr;
      }
      exprType.assign(type);
      final Item item = value(cmp);
      if(item != null) return item;
    }
    return optFirst();
  }
}
