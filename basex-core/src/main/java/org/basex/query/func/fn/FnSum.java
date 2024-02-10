package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.value.type.AtomType.*;

import java.math.*;
import java.util.*;

import org.basex.index.stats.*;
import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.*;
import org.basex.query.expr.path.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public class FnSum extends NumericFn {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item = sum(false, qc);
    return item != null ? item : defined(1) ? arg(1).atomItem(qc, info) : Int.ZERO;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr = opt(false);
    if(expr != null) return expr;

    final Expr values = arg(0), zero = arg(1);
    final SeqType st = values.seqType(), stZero = zero.seqType();
    if(zero == Empty.UNDEFINED) {
      // no default value
      if(st.zero()) return cc.voidAndReturn(values, Int.ZERO, info);
      if(!st.mayBeArray()) {
        final SeqType ost = optType(values, false);
        if(ost != null) exprType.assign(ost);
      }
    } else if(st.zero()) {
      if(zero == Empty.VALUE || stZero.instanceOf(SeqType.ANY_ATOMIC_TYPE_ZO)) {
        return cc.voidAndReturn(values, zero, info);
      }
    } else if(!st.mayBeArray() && !stZero.mayBeArray()) {
      if(st.oneOrMore()) return cc.function(Function.SUM, info, values);
      final SeqType ost = optType(values, false), zst = optType(zero, false);
      final Type type = ost != null && zst != null ? ost.type.union(zst.type) : ANY_ATOMIC_TYPE;
      final Occ occ = stZero.oneOrMore() ? Occ.EXACTLY_ONE : Occ.ZERO_OR_ONE;
      exprType.assign(type, occ);
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
    final Expr values = arg(0);
    if(values instanceof RangeSeq) {
      return range((RangeSeq) values, avg);
    } else if(values instanceof SingletonSeq) {
      final SingletonSeq seq = (SingletonSeq) values;
      if(seq.singleItem()) {
        Item item = seq.itemAt(0);
        final Type type = item.type;
        if(type.isUntyped()) item = Dbl.get(item.dbl(info));
        if(type.isNumber()) return avg ? item : Calc.MULTIPLY.eval(item, Int.get(seq.size()), info);
      }
    } else if(values instanceof Path) {
      final ArrayList<Stats> list = ((Path) values).pathStats();
      if(list != null) {
        double sum = 0;
        long count = 0;
        for(final Stats stats : list) {
          if(!StatsType.isNumeric(stats.type) || !StatsType.isCategory(stats.type)) return this;
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
  protected final void simplifyArgs(final CompileContext cc) throws QueryException {
    final Type type = arg(0).seqType().type;
    if(type.isNumberOrUntyped()) arg(0, arg -> arg.simplifyFor(Simplify.NUMBER, cc));
  }

  /**
   * Computes the result from a range value.
   * @param value sequence
   * @param avg calculate average
   * @return result, or {@code null} if sequence is empty
   * @throws QueryException query exception
   */
  private Item range(final Value value, final boolean avg) throws QueryException {
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
    final Expr values = arg(0);
    if(values instanceof Range) return range(values.value(qc), avg);

    final Iter iter = values.atomIter(qc, info);
    final Item item = iter.next();
    if(item == null) return null;

    Item result = item.type.isUntyped() ? Dbl.get(item.dbl(info)) : item;
    final Type type = result.type;
    final boolean num = result instanceof ANum;
    final boolean dtd = type == DAY_TIME_DURATION, ymd = type == YEAR_MONTH_DURATION;
    if(!num && !dtd && !ymd) throw NUMDUR_X_X.get(info, type, result);

    int c = 1;
    for(Item it; (it = qc.next(iter)) != null;) {
      final Type tp = it.type;
      Type t = null;
      if(tp.isNumberOrUntyped()) {
        if(!num) t = DURATION;
      } else if(num) {
        t = NUMERIC;
      } else if(dtd && tp != DAY_TIME_DURATION || ymd && tp != YEAR_MONTH_DURATION) {
        t = DURATION;
      }
      if(t != null) throw ARGTYPE_X_X_X.get(info, t, tp, it);
      result = Calc.ADD.eval(result, it, info);
      c++;
    }
    return avg ? Calc.DIVIDE.eval(result, Int.get(c), info) : result;
  }
}
