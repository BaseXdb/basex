package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
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
public class FnSum extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq || expr instanceof Range) {
      final Item item = range(expr.value(qc));
      if(item != null) return item;
    } else {
      if(expr instanceof SingletonSeq) {
        final Item item = singleton((SingletonSeq) expr);
        if(item != null) return item;
      }
      final Iter iter = exprs[0].atomIter(qc, info);
      final Item item = iter.next();
      if(item != null) return sum(iter, item, false, qc);
    }

    // return default item
    return exprs.length == 2 ? exprs[1].atomItem(qc, info) : Int.ZERO;
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    // do not simplify summed-up items and zero argument
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs.length == 2 ? exprs[1] : null;
    if(expr1 instanceof RangeSeq) return range((Value) expr1);
    if(expr1 instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr1);
      if(item != null) return item;
    }

    // empty sequence: replace with default item
    final SeqType st1 = expr1.seqType(), st2 = expr2 != null ? expr2.seqType() : null;
    if(st1.zero()) {
      // sequence is empty: check if it also deterministic
      if(!expr1.has(Flag.NDT)) {
        if(expr2 == null) return Int.ZERO;
        if(expr2 == Empty.VALUE) return expr1;
        if(st2.instanceOf(SeqType.ITR_O)) return expr2;
      }
    } else if(st1.oneOrMore() && !st1.mayBeArray()) {
      // sequence is not empty: assign result type
      final Type type1 = st1.type;
      if(type1.isNumber()) exprType.assign(type1.seqType());
      else if(type1.isUntyped()) exprType.assign(SeqType.DBL_O);
    } else if(st2 != null && !st2.zero() && !st2.mayBeArray()) {
      // if input may be empty: consider default argument in static type
      exprType.assign(Calc.PLUS.type(st1.type, st2.type), st2.oneOrMore() ? Occ.ONE : Occ.ZERO_ONE);
    }
    return this;
  }

  /**
   * Compute result from range value.
   * @param value sequence
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  private Item range(final Value value) throws QueryException {
    if(value.isEmpty()) return null;

    // Little Gauss computation
    long min = value.itemAt(0).itr(info), max = value.itemAt(value.size() - 1).itr(info);
    // swap values if order is descending
    if(min > max) {
      final long t = max;
      max = min;
      min = t;
    }

    // range is small enough to be computed with long values
    if(max < 3037000500L) return Int.get((min + max) * (max - min + 1) / 2);
    // compute larger ranges
    final BigInteger bs = BigInteger.valueOf(min), be = BigInteger.valueOf(max);
    final BigInteger bi = bs.add(be).multiply(be.subtract(bs).add(BigInteger.ONE)).
        divide(BigInteger.valueOf(2));
    final long l = bi.longValue();
    // check if result is small enough to be represented as long value
    if(bi.equals(BigInteger.valueOf(l))) return Int.get(l);
    throw RANGE_X.get(info, bi);
  }

  /**
   * Compute result from singleton value.
   * @param seq singleton sequence
   * @return result, or {@code null} if value cannot be evaluated
   * @throws QueryException query exception
   */
  private Item singleton(final SingletonSeq seq) throws QueryException {
    Item item = seq.itemAt(0);
    if(item.type.isUntyped()) item = Dbl.get(item.dbl(info));
    return item.type.isNumber() ? Calc.MULT.eval(item, Int.get(seq.size()), info) : null;
  }

  /**
   * Sums up the specified item(s).
   * @param iter iterator
   * @param item first item
   * @param avg calculate average
   * @param qc query context
   * @return summed up item
   * @throws QueryException query exception
   */
  Item sum(final Iter iter, final Item item, final boolean avg, final QueryContext qc)
      throws QueryException {

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final boolean num = result instanceof ANum, dtd = result.type == DTD, ymd = result.type == YMD;
    if(!num && !dtd && !ymd) throw SUM_X_X.get(info, result.type, result);

    int c = 1;
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type type = it.type;
      Type tp = null;
      if(type.isNumberOrUntyped()) {
        if(!num) tp = DUR;
      } else {
        if(num) tp = NUM;
        else if(dtd && type != DTD || ymd && type != YMD) tp = DUR;
      }
      if(tp != null) throw CMP_X_X_X.get(info, tp, type, it);
      result = Calc.PLUS.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIV.eval(result, Int.get(c), info) : result;
  }
}
