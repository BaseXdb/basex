package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;
import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
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
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnSum extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = sum(false, qc);
    return item != null ? item : exprs.length == 2 ? exprs[1].atomItem(qc, info) : Int.ZERO;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = opt(false);
    if(expr != null) return expr;

    final Expr expr1 = exprs[0], expr2 = exprs.length == 2 ? exprs[1] : null;
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

  /**
   * Pre-evaluates a value expression.
   * @param avg calculate average
   * @return optimized expression or {@code null}
   * @throws QueryException query exception
   */
  final Expr opt(final boolean avg) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof RangeSeq) {
      return range((RangeSeq) expr, avg);
    } else if(expr instanceof SingletonSeq) {
      final SingletonSeq seq = (SingletonSeq) expr;
      if(seq.singleItem()) {
        Item item = seq.itemAt(0);
        final Type type = item.type;
        if(type.isUntyped()) item = Dbl.get(item.dbl(info));
        if(type.isNumber()) return avg ? item : Calc.MULT.eval(item, Int.get(seq.size()), info);
      }
    } else if(expr instanceof Path) {
      final ArrayList<Stats> list = ((Path) expr).pathStats(true);
      if(list != null) {
        double sum = 0;
        long count = 0;
        for(final Stats stats : list) {
          for(final byte[] value : stats.values) {
            if(value.length == 0) return null;
            final long c = stats.values.get(value);
            sum += c * Token.toDouble(value);
            count += c;
          }
        }
        return Dbl.get(avg ? sum / count : sum);
      }
    }
    return null;
  }

  @Override
  protected final void simplifyArgs(final CompileContext cc) {
    // do not simplify summed-up items and zero argument
  }

  /**
   * Computes the result from a range value.
   * @param value sequence
   * @param avg calculate average
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  final Item range(final Value value, final boolean avg) throws QueryException {
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
   * Sums up the specified item(s).
   * @param avg calculate average
   * @param qc query context
   * @return summed up item
   * @throws QueryException query exception
   */
  final Item sum(final boolean avg, final QueryContext qc) throws QueryException {
    final Expr expr = exprs[0];
    if(expr instanceof Range) return range(expr.value(qc), avg);

    final Iter iter = expr.atomIter(qc, info);
    final Item item = iter.next();
    if(item == null) return null;

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final boolean num = result instanceof ANum;
    final boolean dtd = result.type == DAY_TIME_DURATION, ymd = result.type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw NUMDUR_X_X.get(info, result.type, result);

    int c = 1;
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type type = it.type;
      Type tp = null;
      if(type.isNumberOrUntyped()) {
        if(!num) tp = DURATION;
      } else if(num) {
        tp = NUMERIC;
      } else if(dtd && type != DAY_TIME_DURATION || ymd && type != YEAR_MONTH_DURATION) {
        tp = DURATION;
      }
      if(tp != null) throw ARGTYPE_X_X_X.get(info, tp, type, it);
      result = Calc.PLUS.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIV.eval(result, Int.get(c), info) : result;
  }
}
