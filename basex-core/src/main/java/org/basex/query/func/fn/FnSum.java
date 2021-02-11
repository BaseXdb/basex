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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FnSum extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq || expr instanceof Range) {
      final Item item = range(expr.value(qc), false);
      if(item != null) return item;
    } else {
      if(expr instanceof SingletonSeq) {
        final Item item = singleton((SingletonSeq) expr, false);
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
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0];
    if(expr1 instanceof RangeSeq) return range((Value) expr1, false);
    if(expr1 instanceof SingletonSeq) {
      final Item item = singleton((SingletonSeq) expr1, false);
      if(item != null) return item;
    }

    final Expr expr2 = exprs.length == 2 ? exprs[1] : null;
    final SeqType st1 = expr1.seqType(), st2 = expr2 != null ? expr2.seqType() : null;
    if(st1.zero()) {
      // sequence is empty: check if it also deterministic
      if(!expr1.has(Flag.NDT)) {
        if(expr2 == null) return Int.ZERO;
        if(expr2 == Empty.VALUE) return expr1;
        if(st2.instanceOf(SeqType.INTEGER_O)) return expr2;
      }
    } else if(st1.oneOrMore() && !st1.mayBeArray()) {
      // sequence is not empty: assign result type
      final Type type1 = st1.type;
      if(type1.isNumber()) exprType.assign(type1.seqType());
      else if(type1.isUntyped()) exprType.assign(SeqType.DOUBLE_O);
    } else if(st2 != null && !st2.zero() && !st2.mayBeArray()) {
      // if input may be empty: consider default argument in static type
      final Occ occ = st2.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
      exprType.assign(Calc.PLUS.type(st1.type, st2.type), occ);
    }
    return this;
  }

  @Override
  protected final void simplifyArgs(final CompileContext cc) {
    // do not simplify summed-up items and zero argument
  }

  /**
   * Compute result from range value.
   * @param value sequence
   * @param avg calculate average
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  protected final Item range(final Value value, final boolean avg) throws QueryException {
    if(value.isEmpty()) return null;

    long min = value.itemAt(0).itr(info), max = value.itemAt(value.size() - 1).itr(info);
    if(avg) {
      final BigDecimal bs = BigDecimal.valueOf(min), be = BigDecimal.valueOf(max);
      return Dec.get(bs.add(be).divide(Dec.BD_2, MathContext.DECIMAL64));
    }

    // Little Gauss computation
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
   * @param avg calculate average
   * @return result, or {@code null} if value cannot be evaluated
   * @throws QueryException query exception
   */
  protected final Item singleton(final SingletonSeq seq, final boolean avg) throws QueryException {
    if(seq.singleItem()) {
      Item item = seq.itemAt(0);
      if(item.type.isUntyped()) item = Dbl.get(item.dbl(info));
      if(item.type.isNumber()) {
        return avg ? item : Calc.MULT.eval(item, Int.get(seq.size()), info);
      }
    }
    return null;
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
  final Item sum(final Iter iter, final Item item, final boolean avg, final QueryContext qc)
      throws QueryException {

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final boolean num = result instanceof ANum;
    final boolean dtd = result.type == DAY_TIME_DURATION, ymd = result.type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw SUM_X_X.get(info, result.type, result);

    int c = 1;
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type type = it.type;
      Type tp = null;
      if(type.isNumberOrUntyped()) {
        if(!num) {
          tp = DURATION;
        }
      } else {
        if(num) {
          tp = NUMERIC;
        } else if(dtd && type != DAY_TIME_DURATION || ymd && type != YEAR_MONTH_DURATION) {
          tp = DURATION;
        }
      }
      if(tp != null) throw CMP_X_X_X.get(info, tp, type, it);
      result = Calc.PLUS.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIV.eval(result, Int.get(c), info) : result;
  }
}
